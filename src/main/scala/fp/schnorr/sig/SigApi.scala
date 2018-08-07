package fp.schnorr.sig

import fp.schnorr.sig.impl.Signer
import scodec.bits.ByteVector

trait SigApi[A] {
  final def sign[F[_]](
    unsigned: ByteVector,
    secretKey: ByteVector
  )(implicit S: Signer[F, A]): F[Signature[A]] =
    S.sign(unsigned, secretKey)

  final def verify[F[_]](
    raw: ByteVector,
    signature: ByteVector,
    publicKey: ByteVector
  )(implicit S: Signer[F, A]): F[Boolean] =
    S.verify(raw, signature, publicKey)
}
