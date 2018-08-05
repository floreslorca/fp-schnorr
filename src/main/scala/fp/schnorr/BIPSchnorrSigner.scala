package fp.schnorr

import java.security.{MessageDigest, Security}

import cats.effect.Sync
import cats.syntax.all._

import fp.schnorr.sig.{ECCurve, Signer}
import fp.schnorr.sig.{Point, Signature}

import org.bouncycastle.jce.provider.BouncyCastleProvider

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
      BigInt(1, s.toArray),
      { n =>
        val bytes = ByteVector(n.toByteArray)
        if (bytes.length <= 32) {
          ByteVector.fill(32 - bytes.length)(0) ++ bytes
        } else bytes.drop(1)
      }
    )

    val point: Encoder[Point] = Encoder(p =>
      for {
        x <- bigInt.encode(p.x)
        y <- if((p.y & 1.toBigInt) === 1.toBigInt) Successful(hex"03") else Successful(hex"02")
      } yield y.toBitVector ++ x
    )

    def encodePoint(p: Point): F[ByteVector] = F.delay(point.encode(p).require.toByteVector)

    def onCurve(p: Point): F[Boolean] =
      F.catchNonFatal((p.y.modPow(2, fieldSize) - p.x.modPow(3, fieldSize)) % fieldSize === 7.toBigInt)

    def sum(p1: Option[Point], p2: Option[Point]): Option[Point] = (p1, p2) match {
      case (None, p2) => p2
      case (p1, None) => p1
      case (Some(p1), Some(p2)) if (p1.x == p2.x && p1.y != p2.y) => None
      case (Some(p1), Some(p2)) => {
        val lam =
          if (p1 == p2)
            (3 * p1.x * p1.x * (p1.y * 2).modPow(fieldSize - 2, fieldSize)) % fieldSize
          else
            ((p2.y - p1.y) * (p2.x - p1.x).modPow(fieldSize - 2, fieldSize)) % fieldSize
        val x3 = (lam * lam - p1.x - p2.x) % fieldSize

        Some(Point(x = x3, y = (lam * (p1.x - x3) - p1.y) % fieldSize))
      }
    }

    def mult(p: Point, n: BigInt): F[Point] = {
      def go(i: Int, r: Option[Point], p: Option[Point]): Option[Point] = {
        if (i == 256)
          r
        else if (((n >> i) & 1) === 1.toBigInt)
          go(i + 1, sum(r, p), sum(p, p))
        else
          go(i + 1, r, sum(p, p))
      }
      F.catchNonFatal(go(0, None, Some(p)).getOrElse(throw new Exception("fail to multiplicate")))
    }

    def sha256(bytes: ByteVector): F[ByteVector] =
      for {
        d <- sha256Instance
        h <- F.delay(d.digest(bytes.toArray))
      } yield ByteVector(h)

    def getG = F.delay {
      val g = EC.params.getG
      Point(x = g.getXCoord.toBigInteger, y = g.getYCoord.toBigInteger)
    }


    def getN = F.delay(BigInt(EC.curveSpec.getOrder))

    def jacobi(n: BigInt) = F.delay(n.modPow((fieldSize - 1) / 2, fieldSize))

    def decodeBigInt(b: ByteVector): F[BigInt] = F.delay(bigInt.decode(b.toBitVector).require.value)

    def encodeBigInt(b: BigInt): F[ByteVector] = F.delay(bigInt.encode(b).require.toByteVector)

  }

  private def calcK(r: Point, k1: BigInt) = for {
    j <- algebra.jacobi(r.y)
    k <- algebra.getN.map(n => if (j =!= 1.toBigInt) n - k1 else k1)
  } yield k

  private def calcE(r: Point, sKey: BigInt, m: ByteVector) = for {
    e0 <- algebra.encodeBigInt(r.x)
    _ = logger.info(s"e0 = hex: ${e0.toHex} num: ${r.x}")
    mul <- algebra.getG.flatMap(algebra.mult(_, sKey))
    _ = logger.info(s"G*skey = $mul")
    e1 <- algebra.encodePoint(mul)
    _ = logger.info(s"e1 = hex: ${e1.toHex} num: ${mul.x}")
    _ = logger.info(s"e2 = hex: ${m.toHex}")
    eh <- algebra.sha256(e0 ++ e1 ++ m)
    _ = logger.info(s"eh = ${eh.toHex}")
    e <- algebra.decodeBigInt(eh)
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
      _ = logger.info(s"k1 = hex: ${hash.toHex} num: $k1")
      r <- algebra.getG.flatMap(algebra.mult(_, k1))
      _ = logger.info(s"R = x: ${r.x} y: ${r.y}")
      e <- calcE(r, skeyNum, unsigned)
      k <- calcK(r, k1)
      _ = logger.info(s"e = $e ; k = $k")
      r0 <- algebra.encodeBigInt(r.x)
      r1 <- algebra.getN.map((k + (e * skeyNum)) % _).flatMap(algebra.encodeBigInt)
      sig = r0 ++ r1
      _ = logger.info(s"signature: ${sig.toHex}")
    } yield Signature[A](sig)

  def verify(
    raw: ByteVector,
    signature: Signature[A],
    publicKey: Point
  ): F[Boolean] = ???
}
