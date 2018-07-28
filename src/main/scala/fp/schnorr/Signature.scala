package fp.schnorr

import scodec.bits.ByteVector

trait Signature[A] {
  def value: ByteVector
}
