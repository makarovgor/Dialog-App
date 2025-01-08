ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "chatbox",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.21",
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.21",
      "com.typesafe.akka" %% "akka-cluster-typed" % "2.6.21",
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % "2.6.21",
      "com.typesafe.akka" %% "akka-remote" % "2.6.21",
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",

      "io.circe" %% "circe-generic" % "0.14.5",
      "ch.qos.logback" % "logback-classic" % "1.2.11"
    )
  )
