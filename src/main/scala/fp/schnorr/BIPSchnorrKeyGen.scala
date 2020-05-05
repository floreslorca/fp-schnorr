package fp.schnorr

import java.security.SecureRandom

import cats.effect.Sync
import cats.syntax.all._

import fp.schnorr.sig.SigKeyPair
import fp.schnorr.sig.impl.SigKeyGen

import scodec.bits.ByteVector

class BIPSchnorrKeyGen[F[_], A](implicit F: Sync[F], algebra: BIPSchnorrAlgebra) extends SigKeyGen[F, A] {

  def generateKeyPair: F[SigKeyPair[A]] = {
    val numr = BigInt(256, new SecureRandom())
    val enum = algebra.encodeBigInt(numr)

    for {
      priv <- buildPrivateKey(enum)
      pubk <- buildPublicKey(priv)
    } yield SigKeyPair(priv, pubk)
  }

  def genPrivKey: F[ByteVector] = generateKeyPair.map(_.privateKey)

  def buildPrivateKey(rawPk: ByteVector): F[ByteVector] = F.catchNonFatal {
    assert(rawPk.length == 32)
    rawPk
  }

  def buildPublicKey(rawPk: ByteVector): F[ByteVector] = {
    val num = algebra.decodeBigInt(rawPk)
    val gen = algebra.getG
    val mul = algebra.mul(gen, num)
    val b   = algebra.encodePoint(mul)
    F.delay(b)
  }
}
