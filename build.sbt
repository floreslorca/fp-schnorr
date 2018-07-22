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
  "org.typelevel" %% "cats-core" % "1.0.1"
)