package fp.schnorr

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import scodec.bits._

class TestSpec
  extends WordSpec
  with Matchers {

  def sign(msg: ByteVector, skey: BigInt, sig: ByteVector) =
    BIPSchnorr.sign[IO](msg, skey).map(_.value shouldEqual sig)

  "Signing" should {
    "return valid" in {
      val msg = hex"0000000000000000000000000000000000000000000000000000000000000000"
      val skey = BigInt("0000000000000000000000000000000000000000000000000000000000000001")
      val sig = hex"787A848E71043D280C50470E8E1532B2DD5D20EE912A45DBDD2BD1DFBF187EF67031A98831859DC34DFFEEDDA86831842CCD0079E1F92AF177F7F22CC1DCED05"

      sign(msg, skey, sig).unsafeRunSync()
    }
  }
}
