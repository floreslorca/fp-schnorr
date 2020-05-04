package fp.schnorr

import org.scash.secp256k1
import cats.effect.IO

import zio.test.DefaultRunnableSpec
import zio.test._

import zio.test.Assertion._
import scodec.bits._

object SigSpec extends DefaultRunnableSpec {
  val spec =
    suite("sigspec")(
      testM("native verify") {
        val privKey =
          hex"0000000000000000000000000000000000000000000000000000000000000001"
        val pubKey =
          hex"0279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798"
        val msg =
          hex"0000000000000000000000000000000000000000000000000000000000000000"
        val exp =
          hex"787A848E71043D280C50470E8E1532B2DD5D20EE912A45DBDD2BD1DFBF187EF67031A98831859DC34DFFEEDDA86831842CCD0079E1F92AF177F7F22CC1DCED05"
        val ans = (for {
          sig <- BIPSchnorr.sign[IO](msg, privKey)
        } yield sig).unsafeRunSync()

        val native = secp256k1
          .verifySchnorr(msg.toArray, ans.value.toArray, pubKey.toArray)

        println(ans)
        assertM(native)(equalTo(true))
      },
      testM("sig") {
        val privKey =
          hex"0000000000000000000000000000000000000000000000000000000000000001"
        val pubKey =
          hex"0279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798"
        val msg =
          hex"0000000000000000000000000000000000000000000000000000000000000000"
        val exp =
          hex"787A848E71043D280C50470E8E1532B2DD5D20EE912A45DBDD2BD1DFBF187EF67031A98831859DC34DFFEEDDA86831842CCD0079E1F92AF177F7F22CC1DCED05"
        val ans = (for {
          sig <- BIPSchnorr.sign[IO](msg, privKey)
        } yield sig).unsafeRunSync()

        val native = secp256k1
          .signSchnorr(msg.toArray, privKey.toArray)
          .map(ByteVector(_))

        println(ans)
        assertM(native)(equalTo(exp))
      }
    )

}
