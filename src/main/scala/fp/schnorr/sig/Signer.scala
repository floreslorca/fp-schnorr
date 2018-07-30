package fp.schnorr.sig

import scodec.bits.ByteVector

trait Signer[F[_], A]{
  def sign(
    unsigned: ByteVector,
    secretKey: ByteVector
  ): F[Signature[A]]

  def verify(
    raw: ByteVector,
    signature: Signature[A],
    publicKey: Point
  ): F[Boolean]

}
