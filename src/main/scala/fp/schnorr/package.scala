package fp

import java.security.{KeyPair, PrivateKey, PublicKey}

package object sig {
  type SigPrivateKey[A] = SigPrivateKey.Repr[A]

  object SigPrivateKey {
    type Repr[A]

    @inline def apply[A](key: PrivateKey): SigPrivateKey[A] = key.asInstanceOf[SigPrivateKey[A]]
    @inline def toJavaPrivateKey[A](key: SigPrivateKey[A]): PrivateKey = key.asInstanceOf[PrivateKey]
  }

  type SigPublicKey[A] = SigPublicKey.Repr[A]
  object SigPublicKey {
    type Repr[A]

    @inline def apply[A](key: PublicKey): SigPublicKey[A] = key.asInstanceOf[SigPublicKey[A]]
    @inline def toJavaPublicKey[A](key: SigPublicKey[A]): PublicKey  = key.asInstanceOf[PublicKey]
  }

  case class SigKeyPair[A](privateKey: SigPrivateKey[A], publicKey: SigPublicKey[A])

  object SigKeyPair {
    def fromKeyPair[A](keypair: KeyPair): SigKeyPair[A] =
      SigKeyPair[A](SigPrivateKey[A](keypair.getPrivate), SigPublicKey[A](keypair.getPublic))
  }
}
