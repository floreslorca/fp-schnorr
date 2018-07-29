package fp.schnorr

import java.security.{MessageDigest, Security}
import cats.Monad
import cats.effect.Sync
import cats.syntax.all._
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.ECPoint
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs.uint32
import spire.math._
import spire.implicits._

class SchnorrSigner[F[_]: Monad, A](implicit F: Sync[F], EC: ECCurve[A])
  extends Signer[F,A] {

  object algebra {
    private val fieldSize = BigInt("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16)

    private val sha256Instance: F[MessageDigest] = F.delay {
      Security.insertProviderAt(new BouncyCastleProvider(), 1)
      MessageDigest.getInstance("SHA-256")
    }

    val pointCodec: Codec[Point] = ???

    def encode(p: Point): F[ByteVector] = F.delay(pointCodec.encode(p).require.toByteVector)

    def decode(b: ByteVector): F[Point] = F.delay(pointCodec.decode(b.toBitVector).require.value)

    def onCurve(p: ByteVector): F[ECPoint] =
      F.catchNonFatal(EC.params.getCurve.decodePoint(p.toArray))

    def sum(p1: ECPoint, p2: ECPoint): F[ECPoint] = F.delay(p1.add(p2))

    def mult(p: ECPoint, n: BigInt): F[ECPoint] = F.delay(p.multiply(n.bigInteger))

    def sha256(bytes: ByteVector): F[ByteVector] =
      for {
        d <- sha256Instance
        h <- F.delay(d.digest(bytes.toArray))
      } yield ByteVector(h)

    def getG = F.delay(EC.params.getG)

    def getN = F.delay(BigInt(EC.curveSpec.getOrder))

    def jacobi(n: BigInt) = F.delay(pow(n, (fieldSize - 1) / 2).mod(fieldSize))

  }

  val bigInt = uint32.xmap[BigInt](BigInt.apply, _.toLong)
  def decodeBigInt(b: ByteVector): F[BigInt] = F.delay(bigInt.decode(b.toBitVector).require.value)
  def encodeBigInt(b: BigInt): F[ByteVector] = F.delay(bigInt.encode(b).require.toByteVector)

  private def toPoint(ec: ECPoint): F[Point] = F.delay(Point(ec.getYCoord.toBigInteger, ec.getXCoord.toBigInteger))

  private def calcK(r: ECPoint, k1: BigInt) = for {
    j <- algebra.jacobi(r.getYCoord.toBigInteger)
    k <- algebra.getN.map(n => if(j != 1) n - k1 else k1)
  } yield k

  private def calcE(r: ECPoint, sKey: BigInt, m: ByteVector): F[BigInt] = for {
    g <- algebra.getG
    e0 <- encodeBigInt(r.getXCoord.toBigInteger)
    e1 <- algebra.mult(g, sKey).flatMap(toPoint).flatMap(algebra.encode)
    e <- algebra.sha256(e0 ++ e1 ++ m).flatMap(decodeBigInt)
  } yield e

  def sign(
    unsigned: ByteVector,
    secretKey: BigInt
  ): F[Signature[A]] = for {
    hash <- encodeBigInt(secretKey).flatMap(bytes => algebra.sha256(bytes ++ unsigned))
    k1 <- algebra.getN.flatMap(n => decodeBigInt(hash))
    r <- algebra.getG.flatMap(g => algebra.mult(g, k1))
    e <- calcE(r, secretKey, unsigned)
    k <- calcK(r, k1)
    r0 <- encodeBigInt(r.getXCoord.toBigInteger)
    r1 <- algebra.getN.map((k + (e * secretKey)) % _).flatMap(encodeBigInt)
  } yield Signature[A](r0 ++ r1)

  def verify(
    raw: ByteVector,
    signature: Signature[A],
    publicKey: Point
  ): F[Boolean] = ???
}
