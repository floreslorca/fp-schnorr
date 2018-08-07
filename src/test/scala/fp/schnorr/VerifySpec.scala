package fp.schnorr

import cats.effect.IO
import cats.implicits._
import org.scalatest.Assertion
import scodec.bits._

class VerifySpec extends TestSuite {

  val testVec4 = TestVector(
    ByteVector.empty,
    hex"03DEFDEA4CDB677750A420FEE807EACF21EB9898AE79B9768766E4FAA04A2D4A34",
    hex"4DF3C3F68FCC83B27E9D42C90431A72499F17875C81A599B566C9889B9696703",
    hex"00000000000000000000003B78CE563F89A0ED9414F5AA28AD0D96D6795F9C6302A8DC32E64E86A333F20EF56EAC9BA30B7246D6D25E22ADB8C6BE1AEB08D49D"
  )

  "Verifying" should {

    "valid verify 1" in {
      (validTests :+ testVec4).map(vec =>
        BIPSchnorr.verify[IO](vec.msg, vec.sig, vec.pubKey)
          .map(_.shouldBe(true))
      )
        .sequence[IO, Assertion]
        .unsafeRunSync()
    }

    "invalid verify 1" in {
      invalidTests.map(vec =>
        BIPSchnorr.verify[IO](vec.msg, vec.sig, vec.pubKey)
          .map(_.shouldBe(false))
      )
        .sequence[IO, Assertion]
        .attempt
        .unsafeRunSync()
    }
  }
}
