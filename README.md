# fp-schnorr
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/floreslorca/fp-schnorr/master/LICENSE)
[![Build Status](https://travis-ci.org/floreslorca/fp-schnorr.svg?branch=master)](https://travis-ci.org/floreslorca/fp-schnorr) 
[![Coverage Status](https://coveralls.io/repos/github/floreslorca/fp-schnorr/badge.svg?branch=master)](https://coveralls.io/github/floreslorca/fp-schnorr?branch=master)

BIP schnorr implementation in FP scala subset

https://github.com/sipa/bips/blob/bip-schnorr/bip-schnorr.mediawiki

This is the first implementation using the JVM and in Functional Programming. This software is for educational purposes only at the moment. Eventually it will track the path followed by the standard and be released shortly after the spec is set

### Usage ###

Example with cats IO monad
```
  import cats.effect.IO
  import fp.schnorr.BIPSchnorr
  
  val privKey = hex"C90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B14E5C7",
  val pubKey = hex"03FAC2114C2FBB091527EB7C64ECB11F8021CB45E8E7809D3C0938E4B8C0E5F84B",
  val msg = hex"5E2D58D8B3BCDF1ABADEC7829054F90DDA9805AAB56C77333024B9D0A508B75C",

  val result: IO[(Signature[BIPSchnorr], Boolean]] = 
      for {
        signed <- BIPSchnorr.sign[IO](msg, privKey)
        verified <- BIPSchnorr.verify[IO](msg, signed, pubKey)
      } yield (signed, verified)
      
  result.unsafeRunAsync()
      
```

### Todo ###

1. Add keygen
2. batch verifycation
3. Increase type safety
4. Improve error reporting
