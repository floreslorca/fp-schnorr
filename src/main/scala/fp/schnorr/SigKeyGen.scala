package fp.schnorr

import fp.sig.{SigKeyPair, SigPrivateKey, SigPublicKey}
import scodec.bits.ByteVector

abstract class SigKeyGen[F[_], A] {

  def generateKeyPair: F[SigKeyPair[A]]

  def buildPrivateKey(rawPk: ByteVector): F[SigPrivateKey[A]]

  def buildPublicKey(rawPk: ByteVector): F[SigPublicKey[A]]

}
