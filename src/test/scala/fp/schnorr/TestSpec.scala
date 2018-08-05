package fp.schnorr

import cats.effect.IO
import org.scalatest.{Matchers, WordSpec}
import scodec.bits._
//import cats.implicits._

class TestSpec
  extends WordSpec
  with Matchers {

  val signerSync = new BIPSchnorrSigner[IO, BIPSchnorr]

  case class TestVector(
    privKey: ByteVector,
    pubKey: ByteVector,
    msg: ByteVector,
    sig: ByteVector
  )

  val testVec1 = TestVector(
    privKey = hex"0000000000000000000000000000000000000000000000000000000000000001",
    pubKey = hex"0279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798",
    msg = hex"0000000000000000000000000000000000000000000000000000000000000000",
    sig = hex"787A848E71043D280C50470E8E1532B2DD5D20EE912A45DBDD2BD1DFBF187EF67031A98831859DC34DFFEEDDA86831842CCD0079E1F92AF177F7F22CC1DCED05"
  )
  val testVec2 = TestVector(
    privKey = hex"B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF",
    pubKey = hex"02DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659",
    msg = hex"243F6A8885A308D313198A2E03707344A4093822299F31D0082EFA98EC4E6C89",
    sig = hex"2A298DACAE57395A15D0795DDBFD1DCB564DA82B0F269BC70A74F8220429BA1D1E51A22CCEC35599B8F266912281F8365FFC2D035A230434A1A64DC59F7013FD"
  )
  val testVec3 = TestVector(
    privKey = hex"C90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B14E5C7",
    pubKey = hex"03FAC2114C2FBB091527EB7C64ECB11F8021CB45E8E7809D3C0938E4B8C0E5F84B",
    msg = hex"5E2D58D8B3BCDF1ABADEC7829054F90DDA9805AAB56C77333024B9D0A508B75C",
    sig = hex"00DA9B08172A9B6F0466A2DEFD817F2D7AB437E0D253CB5395A963866B3574BE00880371D01766935B92D2AB4CD5C8A2A5837EC57FED7660773A05F0DE142380"
  )

  val tests = List(testVec1, testVec2, testVec3)

  "Signing" should {
    "return valid 1" in {
      BIPSchnorr.sign[IO](testVec1.msg, testVec1.privKey).map(_.value shouldEqual testVec1.sig)
        .unsafeRunSync()
    }
    "return valid 2" in {
      BIPSchnorr.sign[IO](testVec2.msg, testVec2.privKey).map(_.value shouldEqual testVec2.sig)
        .unsafeRunSync()
    }
    "return valid 3" in {
      BIPSchnorr.sign[IO](testVec3.msg, testVec3.privKey).map(_.value shouldEqual testVec3.sig)
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
