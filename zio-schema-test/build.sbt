name := "zio-schema-test"

organization := "zamblauskas"

version := "0.1-SNAPSHOT"

scalaVersion := "3.6.3"

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio-schema"          % "1.6.3",
  "dev.zio"       %% "zio-schema-json"     % "1.6.3",
  "dev.zio"       %% "zio-schema-protobuf" % "1.6.3",
  "org.scalatest" %% "scalatest"           % "3.2.19" % Test
)
