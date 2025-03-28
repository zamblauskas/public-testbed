name := "zio-kafka-test"

organization := "org.example"

version := "0.1-SNAPSHOT"

scalaVersion := "3.6.3"

libraryDependencies ++= Seq(
  "dev.zio"  %% "zio-kafka"    % "2.11.0",
  "dev.zio"  %% "zio-json"     % "0.7.39",
  "org.slf4j" % "slf4j-simple" % "2.0.17"
)
