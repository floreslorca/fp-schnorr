package fp.schnorr

import fp.sig.{SigPrivateKey, SigPublicKey}
import scodec.bits.ByteVector

abstract class Signer[F[_], A] {
  def sign(
    unsigned: ByteVector,
    secretKey: SigPrivateKey[A]
  )(implicit S: Signer[F, A]): F[Signature[A]]

  def verify(
    raw: ByteVector,
    signature: Signature[A],
    publicKey: SigPublicKey[A]
  ): F[Boolean]
}
