package fp.schnorr

import cats.effect.IO
import cats.implicits._

import org.scalatest.Assertion

class KeyGenSpec extends TestSuite {

  "Generate Keypair" should {
    "valid generation" in {
      validTests.map(vec =>
        BIPSchnorr.buildPublicKey[IO](vec.privKey)
          .map(_.shouldBe(vec.pubKey))
      )
        .sequence[IO, Assertion]
        .unsafeRunSync()
    }
    "valid generate 1" in {
      BIPSchnorr.buildPublicKey[IO](testVec2.privKey).map(_.shouldBe(testVec2.pubKey)).unsafeRunSync()
    }
  }
}
