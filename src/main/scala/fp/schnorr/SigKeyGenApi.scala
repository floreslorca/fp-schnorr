package fp.schnorr

import scodec.bits.ByteVector

trait SigKeyGenApi[A] {

  final def generateKeyPair[F[_]](
    implicit S: SigKeyGen[F]
  ): F[SigKeyPair] = S.generateKeyPair

  final def buildPrivateKey[F[_]](rawPrivKey: ByteVector)(
    implicit S: SigKeyGen[F]
  ): F[Point] = S.buildPrivateKey(rawPrivKey)

  final def buildPublicKey[F[_]](rawPubkey: ByteVector)(
    implicit S: SigKeyGen[F]
  ): F[Point] = S.buildPublicKey(rawPubkey)

}
