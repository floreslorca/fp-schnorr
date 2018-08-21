package fp.schnorr

import scodec.bits.ByteVector

package object sig {

  case class Point(x: BigInt, y: BigInt)

  case class SigKeyPair[A](
   privateKey: ByteVector,
   publicKey: ByteVector
  )

  case class Signature[A](value: ByteVector)
}
