package fp.schnorr

import java.security.Signature

import tsec.signature.jca.JCASigAlgebra

class SchnorrSig[F[_], A, PubK[_], PrivK[_], Cert[_]]
  extends JCASigAlgebra[F, A, PubK, PrivK, Cert] {
  override type S = SchnorrSig

}
