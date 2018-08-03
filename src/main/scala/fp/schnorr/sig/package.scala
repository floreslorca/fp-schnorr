package fp.schnorr

import scodec.bits.ByteVector

package object sig {

  case class Point(x: BigInt, y: BigInt)

  case class SigKeyPair(
   privateKey: Point,
   publicKey: Point
  )

  case class Signature[A](value: ByteVector)
}
