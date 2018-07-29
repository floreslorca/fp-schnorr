name := "fp-schnorr"

organization := "floreslorca"

version := "0.0.1"

scalaVersion := "2.12.6"

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

libraryDependencies ++= Seq(
  "org.typelevel"    %% "cats-core"     % "1.0.1",
  "org.typelevel"    %% "cats-effect"   % "1.0.0-RC2",
  "org.scodec"       %% "scodec-core"   % "1.10.3",
  "org.typelevel"    %% "spire"         % "0.14.1",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.60",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)