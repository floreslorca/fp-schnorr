package fp

import scodec.bits.ByteVector

package object schnorr {

  case class Point(y: BigInt, x: BigInt)

  case class SigKeyPair(
   privateKey: Point,
   publicKey: Point
  )

  case class Signature[A](value: ByteVector)
}
