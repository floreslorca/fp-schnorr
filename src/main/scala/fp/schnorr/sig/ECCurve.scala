package fp.schnorr.sig

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec

trait ECCurve[A] {
  protected val cName: String

  lazy val params = ECNamedCurveTable.getParameterSpec(cName)

  lazy val curveSpec: ECNamedCurveSpec = {

    new ECNamedCurveSpec(
      cName,
      params.getCurve,
      params.getG,
      params.getN,
      params.getH
    )
  }

}