package fp.schnorr

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import scodec.bits._

class TestSpec
  extends WordSpec
  with Matchers {

  val signerSync = new BIPSchnorrSigner[IO, BIPSchnorr]

  "Signing" should {
    "return valid" in {
      val msg = hex"0000000000000000000000000000000000000000000000000000000000000000"
      val skey = hex"0000000000000000000000000000000000000000000000000000000000000001"
      val sig = hex"787A848E71043D280C50470E8E1532B2DD5D20EE912A45DBDD2BD1DFBF187EF67031A98831859DC34DFFEEDDA86831842CCD0079E1F92AF177F7F22CC1DCED05"

      BIPSchnorr.sign[IO](msg, skey).map(_.value shouldEqual sig)
        .unsafeRunSync()
    }
  }

  "Encode bigint" should {
    "return valid" in {
      val skey = hex"B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF"

      (
        for {
          skNum <- signerSync.algebra.decodeBigInt(skey)
          skDec <- signerSync.algebra.encodeBigInt(skNum)
        } yield skDec shouldEqual skey
      ).unsafeRunSync()
    }
  }

}
