package fp.schnorr.sig.impl

import fp.schnorr.sig.SigKeyPair
import scodec.bits.ByteVector

abstract class SigKeyGen[F[_], A] {

  def generateKeyPair: F[SigKeyPair[A]]

  def buildPrivateKey(rawPk: ByteVector): F[ByteVector]

  def buildPublicKey(rawPk: ByteVector): F[ByteVector]

  def genPrivKey: F[ByteVector]

}
