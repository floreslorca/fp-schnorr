package fp.schnorr

import cats.effect.IO

class VerifySpec extends TestSuite {

  "Verifying" should {

    "return valid 1" in {
      BIPSchnorr.verify[IO](testVec1.msg, testVec1.sig, testVec1.pubKey)
        .map(_.shouldBe(true))
//        .unsafeRunSync()
    }
    /*
    "return valid 2" in {
      BIPSchnorr.verify[IO](testVec2.msg, testVec2.sig, testVec2.pubKey)
        .map(_.shouldBe(true))
        .unsafeRunSync()
    }

    "return valid 3" in {
      BIPSchnorr.verify[IO](testVec3.msg, testVec3.sig, testVec3.pubKey)
        .map(_.shouldBe(true))
        .unsafeRunSync()
    }
    */
  }
}
