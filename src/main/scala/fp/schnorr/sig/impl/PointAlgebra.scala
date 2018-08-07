package fp.schnorr.sig.impl

import fp.schnorr.sig.Point

import scodec.bits.ByteVector

trait PointAlgebra[F[_], A] {

  def mul(p: Point, b: BigInt): F[Point]

  def add(p1: Option[Point], p2: Option[Point]): F[Option[Point]]

  def onCurve(p: Point): F[Boolean]

  def getG: F[Point]

  def getP: F[BigInt]

  def getN: F[BigInt]

  def jacobi(n: BigInt): F[BigInt]

  def decodePoint(b: ByteVector): F[Point]

  def encodePoint(p: Point): F[ByteVector]

  def decodeBigInt(b: ByteVector): F[BigInt]

  def encodeBigInt(b: BigInt): F[ByteVector]
}
