package fp.schnorr

import java.security.spec.ECGenParameterSpec

trait ECCurve[A] {

  def curve: String

  def keySpecFromCurve: ECGenParameterSpec = new ECGenParameterSpec(curve)
}

