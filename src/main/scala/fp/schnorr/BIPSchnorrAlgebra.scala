package fp.schnorr

import cats.effect.Sync

import fp.schnorr.sig.Point
import fp.schnorr.sig.impl.PointAlgebra

import scodec.Attempt.Successful
import scodec.{Codec, DecodeResult}
import scodec.bits._
import scodec.codecs._

import spire.implicits._

class BIPSchnorrAlgebra[F[_], A](implicit F: Sync[F]) extends PointAlgebra[F, A]{

  private val fieldSize = BigInt("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16)

  private val generator = Point(
    BigInt("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
    BigInt("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
  )

  private val curveOrder = BigInt("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16)

  val bigInt = bytes(32).xmap[BigInt](s =>
    BigInt(1, s.toArray),
    { n =>
      val bytes = ByteVector(n.toByteArray)
      if (bytes.length <= 32) {
        ByteVector.fill(32 - bytes.length)(0) ++ bytes
      } else bytes.drop(1)
    }
  )

  val compressedPoint: Codec[Point] = {
    def encode(p: Point) = for {
      x <- bigInt.encode(p.x)
      y <- if (util.isOdd(p.y)) Successful(hex"03") else Successful(hex"02")
    } yield y.toBitVector ++ x

    def decode(bits: BitVector) = for {
      b <- uint8.decode(bits)
      x <- bigInt.decode(b.remainder)
      exp = (fieldSize + 1)/4
      y2 = x.value.pow(3) + 7.toBigInt
      y1 = y2.modPow(exp, fieldSize) //secp256k1 sqrt equivalent
      y = if((b.value == 2 && util.isOdd(y1)) || (b.value == 3 && !util.isOdd(y1))) fieldSize - y1 else y1
    } yield DecodeResult(Point(x.value, y), x.remainder)

    Codec[Point](encode _, decode _)
  }

  def encodePoint(p: Point): F[ByteVector] = F.delay(compressedPoint.encode(p).require.toByteVector)

  def decodePoint(b: ByteVector): F[Point] = F.delay(compressedPoint.decode(b.toBitVector).require.value)

  def onCurve(p: Point): F[Boolean] =
    F.catchNonFatal((p.y.modPow(2, fieldSize) - p.x.modPow(3, fieldSize)).mod(fieldSize) === 7.toBigInt)

  def add(p1: Option[Point], p2: Option[Point]): F[Option[Point]] = F.delay(addImpl(p1, p2))

  def mul(p: Point, n: BigInt): F[Point] = {
    def go(i: Int, r: Option[Point], p: Option[Point]): Option[Point] = {
      if (i == 256)
        r
      else if (((n >> i) & 1).isOne)
        go(i + 1, addImpl(r, p), addImpl(p, p))
      else
        go(i + 1, r, addImpl(p, p))
    }
    F.catchNonFatal(go(0, None, Some(p)).getOrElse(throw new Exception("fail to multiplicate")))
  }

  private def addImpl(p1: Option[Point], p2: Option[Point]): Option[Point] = (p1, p2) match {
    case (None, p2) => p2
    case (p1, None) => p1
    case (Some(p1), Some(p2)) if (p1.x == p2.x && p1.y != p2.y) => None
    case (Some(p1), Some(p2)) => {
      val lam =
        if (p1 == p2)
          (3 * p1.x * p1.x * (p1.y * 2).modPow(fieldSize - 2, fieldSize)).mod(fieldSize)
        else
          ((p2.y - p1.y) * (p2.x - p1.x).modPow(fieldSize - 2, fieldSize)).mod(fieldSize)
      val x3 = (lam * lam - p1.x - p2.x).mod(fieldSize)

      Some(Point(x = x3, y = (lam * (p1.x - x3) - p1.y).mod(fieldSize)))
    }
  }

  def getG = F.delay(generator)

  def getP = F.delay(fieldSize)

  def getN = F.delay(curveOrder)

  def jacobi(n: BigInt) = F.delay(n.modPow((fieldSize - 1) / 2, fieldSize))

  def decodeBigInt(b: ByteVector): F[BigInt] = F.delay(bigInt.decode(b.toBitVector).require.value)

  def encodeBigInt(b: BigInt): F[ByteVector] = F.delay(bigInt.encode(b).require.toByteVector)

}
