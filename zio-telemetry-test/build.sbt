name := "zio-telemetry-test"

organization := "org.example"

version := "0.1-SNAPSHOT"

scalaVersion := "3.6.3"

libraryDependencies ++= Seq(
  "dev.zio"                 %% "zio-opentelemetry"                   % "3.1.2",
  "io.opentelemetry"         % "opentelemetry-sdk"                   % "1.48.0",
  "io.opentelemetry"         % "opentelemetry-exporter-otlp"         % "1.48.0",
  "io.opentelemetry"         % "opentelemetry-exporter-logging-otlp" % "1.48.0",
  "io.opentelemetry.semconv" % "opentelemetry-semconv"               % "1.30.0"
)
