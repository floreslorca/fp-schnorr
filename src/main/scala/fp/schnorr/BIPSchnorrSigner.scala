package fp.schnorr

import java.security.{MessageDigest, Security}

import cats.effect.Sync
import cats.syntax.all._

import fp.schnorr.sig.{Signer, ECCurve}
import fp.schnorr.sig.{Signature, Point}

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.ECPoint

import scodec.Attempt.Successful
import scodec.Encoder
import scodec.codecs._
import scodec.bits._

import spire.math._
import spire.implicits._

class BIPSchnorrSigner[F[_], A](implicit F: Sync[F], EC: ECCurve[A])
  extends Signer[F,A] {

  object algebra {
    private val fieldSize = BigInt("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16)

    private val sha256Instance: F[MessageDigest] = F.delay {
      Security.insertProviderAt(new BouncyCastleProvider(), 1)
      MessageDigest.getInstance("SHA-256")
    }
    val bigInt = bytes(32).xmap[BigInt](s =>
      BigInt(s.toArray), n => ByteVector(n.toByteArray)
    )

    val point: Encoder[Point] = Encoder(p =>
      for {
        y <- if((p.y & 1.toBigInt) === 1.toBigInt) Successful(hex"0x03") else Successful(hex"0x02")
        x <- bigInt.encode(p.x)
      } yield y.toBitVector ++ x
    )

    def encode(p: Point): F[ByteVector] = F.delay(point.encode(p).require.toByteVector)

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

    def decodeBigInt(b: ByteVector): F[BigInt] = F.delay(bigInt.decode(b.toBitVector).require.value)

    def encodeBigInt(b: BigInt): F[ByteVector] = F.delay(bigInt.encode(b).require.toByteVector)

  }

  private def toPoint(ec: ECPoint): F[Point] = F.delay(Point(ec.getYCoord.toBigInteger, ec.getXCoord.toBigInteger))

  private def calcK(r: ECPoint, k1: BigInt) = for {
    j <- algebra.jacobi(r.getYCoord.toBigInteger)
    k <- algebra.getN.map(n => if (j =!= 1.toBigInt) n - k1 else k1)
  } yield k

  private def calcE(r: ECPoint, sKey: BigInt, m: ByteVector) = for {
    g <- algebra.getG
    e0 <- algebra.encodeBigInt(r.getXCoord.toBigInteger)
    e1 <- algebra.mult(g, sKey).flatMap(toPoint).flatMap(algebra.encode)
    e <- algebra.sha256(e0 ++ e1 ++ m).flatMap(algebra.decodeBigInt)
  } yield e

  def sign(
    unsigned: ByteVector,
    secretKey: ByteVector
  ): F[Signature[A]] =
    for {
      skeyNum <- algebra.decodeBigInt(secretKey)
      hash <- algebra.sha256(secretKey ++ unsigned)
      k1 <- algebra.getN.flatMap(n => algebra.decodeBigInt(hash))
      r <- algebra.getG.flatMap(g => algebra.mult(g, k1))
      e <- calcE(r, skeyNum, unsigned)
      k <- calcK(r, k1)
      r0 <- algebra.encodeBigInt(r.getXCoord.toBigInteger)
      r1 <- algebra.getN.map((k + (e * skeyNum)) % _).flatMap(algebra.encodeBigInt)
    } yield Signature[A](r0 ++ r1)

  def verify(
    raw: ByteVector,
    signature: Signature[A],
    publicKey: Point
  ): F[Boolean] = ???
}
