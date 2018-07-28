package fp.schnorr

import fp.sig.{SigKeyPair, SigPrivateKey, SigPublicKey}
import scodec.bits.ByteVector

trait SigKeyGenApi[A] {

  final def generateKeyPair[F[_]](
    implicit S: SigKeyGen[F, A]
  ): F[SigKeyPair[A]] = S.generateKeyPair

  final def buildPrivateKey[F[_]](rawPrivKey: ByteVector)(
    implicit S: SigKeyGen[F, A]
  ): F[SigPrivateKey[A]] = S.buildPrivateKey(rawPrivKey)

  final def buildPublicKey[F[_]](rawPubkey: ByteVector)(
    implicit S: SigKeyGen[F, A]
  ): F[SigPublicKey[A]] = S.buildPublicKey(rawPubkey)

}
