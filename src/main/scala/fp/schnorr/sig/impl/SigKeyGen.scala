package fp.schnorr.sig.impl

import fp.schnorr.sig.{Point, SigKeyPair}
import scodec.bits.ByteVector

abstract class SigKeyGen[F[_]] {

  def generateKeyPair: F[SigKeyPair]

  def buildPrivateKey(rawPk: ByteVector): F[Point]

  def buildPublicKey(rawPk: ByteVector): F[Point]

}
