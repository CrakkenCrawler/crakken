import com.twitter.sbt._
import sbtbuildinfo.Plugin._

name := "crakken"

organization := "crakken"

version := "0.2.0-SNAPSHOT"

resolvers += "spray repo" at "http://repo.spray.io"

val scalazVersion = "7.0.5"

val akkaVersion = "2.2.4"

val sprayVersion = "1.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-actor"                 % akkaVersion,
  "com.typesafe.akka"     %% "akka-remote"                % akkaVersion,
  "com.typesafe.akka"     %% "akka-slf4j"                 % akkaVersion,
  "io.spray"              %  "spray-client"               % sprayVersion,
  "io.spray"              %  "spray-caching"              % sprayVersion,
  "org.jsoup"             %  "jsoup"                      % "1.7.3",
  "org.reactivemongo"     %% "play2-reactivemongo"        % "0.10.2",
  "org.reactivemongo"     %% "play2-reactivemongo"        % "0.10.2",
  jdbc,
  "com.typesafe.akka"     %% "akka-testkit"               % akkaVersion       % "test",
  "org.scalatest"         %% "scalatest"                  % "2.1.1"           % "test",
  "org.scalaz"            %% "scalaz-scalacheck-binding"  % scalazVersion     % "test"
)

seq(StandardProject.newSettings: _*)

scalacOptions += "-feature"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "crakken"

gitSettings ++ Seq(
  gitTagName <<= (version) map { (v) =>
    "v%s".format(v)
  },
  gitCommitMessage <<= (version) map { (v) =>
    "release commit for %s".format(v)
  }
)

play.Project.playScalaSettings
