package fp.schnorr

import java.security.{MessageDigest, Security}

import cats.effect.Sync
import cats.syntax.all._

import fp.schnorr.sig.{ECCurve, Signer}
import fp.schnorr.sig.{Point, Signature}

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.math.ec.ECPoint

import org.slf4j.LoggerFactory

import scodec.Attempt.Successful
import scodec.Encoder
import scodec.codecs._
import scodec.bits._

import spire.implicits._

class BIPSchnorrSigner[F[_], A](implicit F: Sync[F], EC: ECCurve[A])
  extends Signer[F,A] {

  val logger = LoggerFactory.getLogger("logger")

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

    def jacobi(n: BigInt) = F.delay {
      val num = (fieldSize - 1) / 2
      val e = n.modPow(num, fieldSize)
      logger.info(s" jacobian = $e")
      e
    }

    def decodeBigInt(b: ByteVector): F[BigInt] = F.delay(bigInt.decode(b.toBitVector).require.value)

    def encodeBigInt(b: BigInt): F[ByteVector] = F.delay(bigInt.encode(b).require.toByteVector)

  }

  private def toPoint(ec: ECPoint): F[Point] = F.delay(Point(ec.getYCoord.toBigInteger, ec.getXCoord.toBigInteger))

  private def calcK(r: ECPoint, k1: BigInt) = for {
    j <- algebra.jacobi(r.getYCoord.toBigInteger)
    _ = logger.info(s" jacobian = $j")
    k <- algebra.getN.map(n => if (j =!= 1.toBigInt) n - k1 else k1)
  } yield k

  private def calcE(r: ECPoint, sKey: BigInt, m: ByteVector) = for {
    e0 <- F.delay(ByteVector(r.getXCoord.getEncoded))
    e1 <- algebra.getG.flatMap(algebra.mult(_, sKey))
            .flatMap(toPoint)
            .flatMap(algebra.encode)
    e <- algebra.sha256(e0 ++ e1 ++ m).flatMap(algebra.decodeBigInt)
  } yield e

  def sign(
    unsigned: ByteVector,
    secretKey: ByteVector
  ): F[Signature[A]] =
    for {
      skeyNum <- algebra.decodeBigInt(secretKey)
      _ = logger.info(s"secret = num: $skeyNum hex: ${secretKey.toHex}")
      hash <- algebra.sha256(secretKey ++ unsigned)
      k1 <- algebra.decodeBigInt(hash)
      _ = logger.info(s"k1 = hex: $hash num: $k1")
      r <- algebra.getG.flatMap(algebra.mult(_, k1))
      _ = logger.info(s"Rx = hex: ${r.getXCoord} num: ${r.getXCoord.toBigInteger}")
      e <- calcE(r, skeyNum, unsigned)
      k <- calcK(r, k1)
      _ = logger.info(s"e = $e ; k = $k")
      r0 <- F.delay(ByteVector(r.getXCoord.getEncoded))
      r1 <- algebra.getN.map((k + (e * skeyNum)) % _).flatMap(algebra.encodeBigInt)
      _ = println(s"r0 = hex: ${r0.toHex} r1 = hex: ${r1.toHex}")
    } yield Signature[A](r0 ++ r1)

  def verify(
    raw: ByteVector,
    signature: Signature[A],
    publicKey: Point
  ): F[Boolean] = ???
}
