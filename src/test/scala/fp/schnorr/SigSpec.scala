package fp.schnorr

import java.security.{ MessageDigest, Security }

import org.scash.secp256k1
import cats.effect.IO
import org.bouncycastle.jce.provider.BouncyCastleProvider
import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._
import scodec.bits._
import zio.ZIO
//import zio.ZIO

object SigSpec extends DefaultRunnableSpec {
  Security.insertProviderAt(new BouncyCastleProvider(), 1)
  private val sha256Instance: MessageDigest = MessageDigest.getInstance("SHA-256")

  def sha256(bytes: Array[Byte]): ByteVector = ByteVector(sha256Instance.digest(bytes))

  val spec =
    suite("nativesigspec")(
      testM("sign&nativeVerify")(
        checkM(Gen.listOfN(32)(Gen.anyByte).map(_.toArray)) { bytes =>
          val ans = (for {
            priv <- BIPSchnorr.genPrivateKey[IO]
            sig  <- BIPSchnorr.sign[IO](ByteVector(bytes), priv)
          } yield (sig, priv)).unsafeRunSync()
          assertM(
            secp256k1
              .computePubKey(ans._2.toArray)
              .flatMap(pub => secp256k1.verifySchnorr(bytes, ans._1.value.toArray, pub))
          )(isTrue)
        }
      ),
      testM("signNative&verify")(
        checkM(Gen.listOfN(32)(Gen.anyByte).map(_.toArray)) { bytes =>
          val ans = for {
            // priv <- ZIO.effect(BIPSchnorr.genPrivateKey[IO].unsafeRunSync())
            kp <- ZIO.effect(BIPSchnorr.generateKeyPair[IO].unsafeRunSync())
            //pub  <- secp256k1.computePubKey(priv.toArray)
            sig  <- secp256k1.signSchnorr(bytes, kp.privateKey.toArray)
            npub <- secp256k1.computePubKey(kp.privateKey.toArray)
            //_    = println(s"${kp.publicKey == ByteVector(npub)} ${kp.publicKey} == ${ByteVector(npub)}")
            ver1 <- secp256k1.verifySchnorr(bytes, sig, kp.publicKey.toArray)
            ver2 <- ZIO.effect(BIPSchnorr.verify[IO](ByteVector(bytes), ByteVector(sig), kp.publicKey).unsafeRunSync())
          } yield ver1 && ver2
          assertM(ans)(isTrue)
        }
      )
    )
}
