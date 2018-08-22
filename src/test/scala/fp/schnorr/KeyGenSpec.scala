package fp.schnorr

import cats.effect.IO
import cats.implicits._
import org.scalatest.Assertion
import scodec.bits.ByteVector

class KeyGenSpec extends TestSuite {

  "Generate Keypair" should {
    "build public key from secret" in {
      validTests.map(vec =>
        BIPSchnorr.buildPublicKey[IO](vec.privKey)
          .map(_.shouldBe(vec.pubKey))
      )
        .sequence[IO, Assertion]
        .unsafeRunSync()
    }

    "generate random keypair" in {
      BIPSchnorr.generateKeyPair[IO].map(_.privateKey.length shouldBe 32 ).unsafeRunSync()
    }

    "build secret key from raw bytevector" in {
      validTests.map(vec =>
        BIPSchnorr.buildPrivateKey[IO](vec.privKey)
          .map(_.shouldBe(vec.privKey))
      )
        .sequence[IO, Assertion]
        .unsafeRunSync()
    }


    "attempt to build invalid secret key from raw bytevector" in {
      assertThrows[AssertionError](
        BIPSchnorr.buildPrivateKey[IO](ByteVector.fill(2)(1)).unsafeRunSync()
      )
    }
  }
}
