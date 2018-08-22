package fp.schnorr

import java.security.SecureRandom

import cats.effect.Sync
import cats.syntax.all._
import fp.schnorr.sig.SigKeyPair
import fp.schnorr.sig.impl.SigKeyGen

import scodec.bits.ByteVector

class BIPSchnorrKeyGen[F[_], A](implicit F: Sync[F], algebra: BIPSchnorrAlgebra[F, A]) extends SigKeyGen[F, A] {
  def generateKeyPair: F[SigKeyPair[A]] =  for {
    numr <- F.pure(BigInt(256, new SecureRandom()))
    enum <- algebra.encodeBigInt(numr)
    priv <- buildPrivateKey(enum)
    pubk <- buildPublicKey(priv)
  } yield SigKeyPair(priv, pubk)

  def buildPrivateKey(rawPk: ByteVector): F[ByteVector] = F.catchNonFatal{
    assert(rawPk.length == 32)
    rawPk
  }

  def buildPublicKey(rawPk: ByteVector): F[ByteVector] = for {
    num <- algebra.decodeBigInt(rawPk)
    gen <- algebra.getG
    mul <- algebra.mul(gen, num)
    b <- algebra.encodePoint(mul)
  } yield b
}
