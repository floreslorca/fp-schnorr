package fp.schnorr.sig

import fp.schnorr.sig.impl.SigKeyGen
import scodec.bits.ByteVector

trait SigKeyGenApi[A] {

  final def generateKeyPair[F[_]](
    implicit S: SigKeyGen[F, A]
  ): F[SigKeyPair[A]] = S.generateKeyPair

  final def buildPrivateKey[F[_]](rawPrivKey: ByteVector)(
    implicit S: SigKeyGen[F, A]
  ): F[ByteVector] = S.buildPrivateKey(rawPrivKey)

  final def buildPublicKey[F[_]](rawPubkey: ByteVector)(
    implicit S: SigKeyGen[F, A]
  ): F[ByteVector] = S.buildPublicKey(rawPubkey)

}
