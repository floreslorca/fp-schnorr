package fp.schnorr

import cats.effect.Sync
import fp.schnorr.sig.{ECCurve, Sig}

sealed trait BIPSchnorr

object BIPSchnorr extends Sig[BIPSchnorr]("secp256k1", 64) {

//    implicit def sigKeyGen[F[_]](F: Sync[F]): SigKeyGen[F, B]

  implicit val ecCurve: ECCurve[BIPSchnorr] = this

  implicit def signerSync[F[_]: Sync] = new BIPSchnorrSigner[F, BIPSchnorr]

}

