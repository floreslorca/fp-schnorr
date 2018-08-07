package fp.schnorr

import cats.effect.Sync
import fp.schnorr.sig.Sig

sealed trait BIPSchnorr

object BIPSchnorr extends Sig[BIPSchnorr]{

//    implicit def sigKeyGen[F[_]](F: Sync[F]): SigKeyGen[F, B]

  implicit def curveSync[F[_]: Sync] = new BIPSchnorrAlgebra[F, BIPSchnorr]

  implicit def signerSync[F[_]: Sync] = new BIPSchnorrSigner[F, BIPSchnorr]

}

