package fp.schnorr

import cats.Monad

sealed trait BIPSchnorr

object BIPSchnorr extends SchnorrSig[BIPSchnorr]("secp256k1", 64) {

//    implicit def sigKeyGen[F[_]](F: Sync[F]): SigKeyGen[F, B]

  implicit def signerSync[F[_]: Monad] = new SchnorrSigner[F, BIPSchnorr]

}

