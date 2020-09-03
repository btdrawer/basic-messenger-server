name := "basic-messenger-server"

version := "0.1"

scalaVersion := "2.13.3"

lazy val AkkaVersion = "2.6.8"
lazy val AkkaHttpVersion = "10.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "io.spray" %%  "spray-json" % "1.3.5"
)
