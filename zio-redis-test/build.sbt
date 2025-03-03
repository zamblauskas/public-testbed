name := "zio-redis-test"

organization := "org.example"

version := "0.1-SNAPSHOT"

scalaVersion := "3.6.3"

libraryDependencies ++= Seq(
  "dev.zio"       %% "zio-redis"           % "1.1.0",
  "dev.zio"       %% "zio-json"            % "0.7.32",
  "dev.zio"       %% "zio-schema"          % "1.6.3",
  "dev.zio"       %% "zio-schema-json"     % "1.6.3",
  "dev.zio"       %% "zio-schema-protobuf" % "1.6.3",
  "org.scalatest" %% "scalatest"           % "3.2.19" % Test
)
