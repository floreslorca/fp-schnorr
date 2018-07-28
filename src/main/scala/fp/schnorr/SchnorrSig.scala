package fp.schnorr

abstract class SchnorrSig[A](curveName: String)
  extends ECCurve[A]
  with SigKeyGenApi[A]
  with SigApi[A] {
  override protected val cName = curveName
}
