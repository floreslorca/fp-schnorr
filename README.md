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
  
  val result: IO[(Signature[BIPSchnorr], Boolean]] = 
      for {
        kp <- BIPSchnorr.generateKeyPair[IO]
        signed <- BIPSchnorr.sign[IO](msg, kp.privKey)
        verified <- BIPSchnorr.verify[IO](msg, signed, kp.pubKey)
      } yield (signed, verified)
      
  result.unsafeRunAsync() // executes and returns the pair value
      
```

### Todo ###

1. batch verification
3. Increase type safety
4. Improve error reporting
