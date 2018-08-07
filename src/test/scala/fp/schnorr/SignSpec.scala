package fp.schnorr

import cats.effect.IO
import cats.implicits._

class SignSpec extends TestSuite {
  "Signing" should {

    "return valid 1" in {
      validTests.map(vec =>
      BIPSchnorr.sign[IO](vec.msg, vec.privKey)
        .map(_.value shouldEqual vec.sig)
      )
        .sequence
        .unsafeRunSync()
    }
  }
}
