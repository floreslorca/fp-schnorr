package fp.schnorr

import java.security.{ MessageDigest, Security }

import cats.effect.Sync
import cats.syntax.all._

import fp.schnorr.sig.impl.Signer
import fp.schnorr.sig.{ Point, Signature }
import org.bouncycastle.jce.provider.BouncyCastleProvider

import scodec.bits._
import spire.implicits._

class BIPSchnorrSigner[F[_], A](implicit F: Sync[F], algebra: BIPSchnorrAlgebra) extends Signer[F, A] {
  Security.insertProviderAt(new BouncyCastleProvider(), 1)
  private val sha256Instance: MessageDigest = MessageDigest.getInstance("SHA-256")

  def sha256(bytes: ByteVector): ByteVector = ByteVector(sha256Instance.digest(bytes.toArray))

  private def calcK(r: Point, k1: BigInt): BigInt =
    if (algebra.jacobi(r.y) != 1.toBigInt) algebra.getN - k1
    else k1

  private def calcE(r: Point, sKey: BigInt, m: ByteVector): BigInt = {
    val e0  = algebra.encodeBigInt(r.x)
    val mul = algebra.mul(algebra.getG, sKey)
    val e1  = algebra.encodePoint(mul)
    val eh  = sha256(e0 ++ e1 ++ m)
    val e   = algebra.decodeBigInt(eh)
    e
  }

  def sign(
    unsigned: ByteVector,
    secretKey: ByteVector
  ): F[Signature[A]] = {
    val skeyNum = algebra.decodeBigInt(secretKey)
    val hash    = sha256(secretKey ++ unsigned)
    val k1      = algebra.decodeBigInt(hash)
    val r       = algebra.mul(algebra.getG, k1)
    val e       = calcE(r, skeyNum, unsigned)
    val k       = calcK(r, k1)
    val r0      = algebra.encodeBigInt(r.x)
    val r1      = algebra.encodeBigInt((k + (e * skeyNum)).mod(algebra.getN))
    val sig     = r0 ++ r1
    F.delay(Signature[A](sig))
  }

  def verify(
    msg: ByteVector,
    signature: ByteVector,
    publicKey: ByteVector
  ): F[Boolean] = {
    def verifyImpl(pk: Point) = {
      val rs = signature.splitAt(32)
      val r  = algebra.decodeBigInt(rs._1)
      val s  = algebra.decodeBigInt(rs._2)
      val n  = algebra.getN
      val p  = algebra.getP
      val ans =
        if (r >= p || s >= n) F.delay(false)
        else {
          val g    = algebra.getG
          val e    = algebra.decodeBigInt(sha256(rs._1 ++ publicKey ++ msg)) //BUG: must be compressed
          val gs   = algebra.mul(g, s)
          val pne  = algebra.mul(pk, n - e)
          val bigR = algebra.add(gs.some, pne.some)
          val ver  = bigR.fold(false)(bigR => if (algebra.jacobi(bigR.y) != 1.toBigInt || bigR.x != r) false else true)
          F.delay(ver)
        }
      ans
    }

    val pk  = algebra.decodePoint(publicKey)
    val on  = algebra.onCurve(pk)
    val ans = if (on) verifyImpl(pk) else F.pure(false)
    ans
  }
}
