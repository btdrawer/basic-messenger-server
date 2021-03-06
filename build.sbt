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
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,
  "com.zaxxer" % "HikariCP" % "3.4.5",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "io.spray" %%  "spray-json" % "1.3.5",
  "org.postgresql" % "postgresql" % "42.2.16"
)
