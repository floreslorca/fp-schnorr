package fp.schnorr

import scodec.bits.ByteVector

package object sig {

  case class Point(y: BigInt, x: BigInt)

  case class SigKeyPair(
   privateKey: Point,
   publicKey: Point
  )

  case class Signature[A](value: ByteVector)
}
