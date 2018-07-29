package fp.schnorr

import scodec.bits.ByteVector

trait SigApi[A] {
  final def sign[F[_]](
    unsigned: ByteVector,
    secretKey: BigInt
  )(implicit S: Signer[F, A]): F[Signature[A]] =
    S.sign(unsigned, secretKey)

  final def verify[F[_]](
    raw: ByteVector,
    signature: Signature[A],
    publicKey: Point
  )(implicit S: Signer[F, A]): F[Boolean] =
    S.verify(raw, signature, publicKey)
}
