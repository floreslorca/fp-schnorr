package fp.schnorr.sig

abstract class Sig[A](curveName: String, outLen: Int)
  extends ECCurve[A]
  //with SigKeyGenApi[A]
  with SigApi[A] {
  override protected val cName = curveName
}
