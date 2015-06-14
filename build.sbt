name := "Refine"

version := "1.0"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-log4j12" % "1.7.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "joda-time" % "joda-time" % "2.8",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)
