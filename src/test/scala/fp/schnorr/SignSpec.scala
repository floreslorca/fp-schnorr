package fp.schnorr

import cats.effect.IO

class SignSpec extends TestSuite {
  "Signing" should {

    "return valid 1" in {
      BIPSchnorr.sign[IO](testVec1.msg, testVec1.privKey)
        .map(_.value shouldEqual testVec1.sig)
        .unsafeRunSync()
    }

    "return valid 2" in {
      BIPSchnorr.sign[IO](testVec2.msg, testVec2.privKey)
        .map(_.value shouldEqual testVec2.sig)
        .unsafeRunSync()
    }

    "return valid 3" in {
      BIPSchnorr.sign[IO](testVec3.msg, testVec3.privKey)
        .map(_.value shouldEqual testVec3.sig)
        .unsafeRunSync()
    }

  }
}
