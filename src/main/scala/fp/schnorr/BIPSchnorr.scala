package fp.schnorr

import cats.effect.Sync
import fp.schnorr.sig.Sig

sealed trait BIPSchnorr

object BIPSchnorr extends Sig[BIPSchnorr] {

  implicit def algebraSync = new BIPSchnorrAlgebra

  implicit def sigKeyGen[F[_]: Sync] = new BIPSchnorrKeyGen[F, BIPSchnorr]

  implicit def signerSync[F[_]: Sync] = new BIPSchnorrSigner[F, BIPSchnorr]

}
