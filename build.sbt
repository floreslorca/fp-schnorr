name := "fp-schnorr"

organization := "floreslorca"

version := "0.2.0"

scalaVersion := "2.12.10"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-Yno-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

lazy val ziov = "1.0.0-RC18-2+147-6dcf6568-SNAPSHOT"
lazy val ziosecp256k1v = "0.1.5"

resolvers ++= Seq(
  Opts.resolver.sonatypeSnapshots,
  Resolver.bintrayRepo("scala-cash", "io")
)

libraryDependencies ++= Seq(
  "org.bouncycastle" % "bcprov-jdk15on" % "1.65",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.typelevel" %% "spire" % "0.14.1",
  "org.scodec" %% "scodec-core" % "1.10.3",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "dev.zio" %% "zio" % ziov,
  "dev.zio" %% "zio-test" % ziov,
  "dev.zio" %% "zio-test-sbt" % ziov,
  "org.scash" %% "zio-secp256k1" % ziosecp256k1v
)
