package fp.schnorr.sig.impl

import fp.schnorr.sig.Signature
import scodec.bits.ByteVector

trait Signer[F[_], A]{
  def sign(
    unsigned: ByteVector,
    secretKey: ByteVector
  ): F[Signature[A]]

  def verify(
    raw: ByteVector,
    signature: ByteVector,
    publicKey: ByteVector
  ): F[Boolean]

}
