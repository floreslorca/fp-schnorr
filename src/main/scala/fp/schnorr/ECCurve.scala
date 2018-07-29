package fp.schnorr

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.math.ec.custom.sec.SecP256K1FieldElement

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

  val e = new SecP256K1FieldElement
}