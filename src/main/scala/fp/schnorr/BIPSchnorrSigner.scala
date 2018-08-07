package fp.schnorr

import java.security.{MessageDigest, Security}

import cats.effect.Sync
import cats.syntax.all._

import fp.schnorr.sig.impl.Signer
import fp.schnorr.sig.{Point, Signature}
import org.bouncycastle.jce.provider.BouncyCastleProvider

import scodec.bits._
import spire.implicits._

class BIPSchnorrSigner[F[_], A](implicit F: Sync[F], algebra: BIPSchnorrAlgebra[F, A]) extends Signer[F,A] {

  private val sha256Instance: F[MessageDigest] = F.delay {
    Security.insertProviderAt(new BouncyCastleProvider(), 1)
    MessageDigest.getInstance("SHA-256")
  }

  def sha256(bytes: ByteVector): F[ByteVector] =
    for {
      d <- sha256Instance
      h <- F.delay(d.digest(bytes.toArray))
    } yield ByteVector(h)

  private def calcK(r: Point, k1: BigInt) = for {
    j <- algebra.jacobi(r.y)
    k <- algebra.getN.map(n => if (j =!= 1.toBigInt) n - k1 else k1)
  } yield k

  private def calcE(r: Point, sKey: BigInt, m: ByteVector) = for {
    e0 <- algebra.encodeBigInt(r.x)
    mul <- algebra.getG.flatMap(algebra.mul(_, sKey))
    e1 <- algebra.encodePoint(mul)
    eh <- sha256(e0 ++ e1 ++ m)
    e <- algebra.decodeBigInt(eh)
  } yield e

  def sign(
    unsigned: ByteVector,
    secretKey: ByteVector
  ): F[Signature[A]] =
    for {
      skeyNum <- algebra.decodeBigInt(secretKey)
      hash <- sha256(secretKey ++ unsigned)
      k1 <- algebra.decodeBigInt(hash)
      r <- algebra.getG.flatMap(algebra.mul(_, k1))
      e <- calcE(r, skeyNum, unsigned)
      k <- calcK(r, k1)
      r0 <- algebra.encodeBigInt(r.x)
      r1 <- algebra.getN.map((k + (e * skeyNum)).mod _).flatMap(algebra.encodeBigInt)
      sig = r0 ++ r1
    } yield Signature[A](sig)

  def verify(
    msg: ByteVector,
    signature: ByteVector,
    publicKey: ByteVector
  ): F[Boolean] = {
    def verifyImpl(pk: Point) =
      for {
        rs <- F.delay(signature.splitAt(32))
        r <- algebra.decodeBigInt(rs._1)
        s <- algebra.decodeBigInt(rs._2)
        n <- algebra.getN
        p <- algebra.getP
        ans <-
          if(r >= p || s >= n) {
            F.delay(false)
          } else {
            for {
              g <- algebra.getG
              e <- sha256(rs._1 ++ publicKey ++ msg).flatMap(algebra.decodeBigInt)
              gs <- algebra.mul(g, s)
              pne <- algebra.mul(pk, n - e)
              bigR <- algebra.add(gs.some, pne.some)
              ver <- bigR.fold(F.delay(false)){ bigR =>
                algebra.jacobi(bigR.y).map(jac => if(jac != 1.toBigInt || bigR.x != r) false else true)
              }
          } yield ver
        }
      } yield ans

      for {
        pk <- algebra.decodePoint(publicKey)
        on <- algebra.onCurve(pk)
        ans <- if(on) verifyImpl(pk) else F.delay(false)
      } yield ans
  }
}
