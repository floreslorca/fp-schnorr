package fp.schnorr

import cats.implicits._

class CodecSpec extends TestSuite {

  "Roundtrip" should {
    "return valid 1" in {
      validTests.map(vec =>
        for {
          skNum <- curveSync.decodeBigInt(vec.privKey)
          skDec <- curveSync.encodeBigInt(skNum)
        } yield skDec shouldEqual vec.privKey
      )
        .sequence
        .unsafeRunSync()
    }
  }

}
