package fp.schnorr

import fp.sig.{SigPrivateKey, SigPublicKey}
import scodec.bits.ByteVector

trait SigApi[A] {
  final def sign[F[_]](
    unsigned: ByteVector,
    secretKey: SigPrivateKey[A]
  )(implicit S: Signer[F, A]): F[Signature[A]] =
    S.sign(unsigned, secretKey)

  final def verify[F[_]](
    raw: ByteVector,
    signature: Signature[A],
    publicKey: SigPublicKey[A]
  )(implicit S: Signer[F, A]): F[Boolean] =
    S.verify(raw, signature, publicKey)
}
