package fp.schnorr

import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters

trait ECCurve[A] {
  protected val cName: String

  val params = SECNamedCurves.getByName(cName)

  def curve = new ECDomainParameters(
    params.getCurve,
    params.getG,
    params.getN,
    params.getH
  )
}