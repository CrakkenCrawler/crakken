import com.twitter.sbt._
import sbtbuildinfo.Plugin._

name := "crakken"

organization := "crakkencrawler"

version := "0.1.8-SNAPSHOT"

resolvers += "spray repo" at "http://repo.spray.io"

val scalazVersion = "7.0.5"

val akkaVersion = "2.2.4"

val sprayVersion = "1.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-actor"                 % akkaVersion,
  "com.typesafe.akka"     %% "akka-remote"                % akkaVersion,
  "com.typesafe.akka"     %% "akka-slf4j"                 % akkaVersion,
  "com.typesafe.play"     %% "play-slick"                 % "0.6.0.1",
  "io.spray"              %  "spray-client"               % sprayVersion,
  "io.spray"              %  "spray-caching"              % sprayVersion,
  "org.jsoup"             %  "jsoup"                      % "1.7.3",
  "org.scalaz"            %% "scalaz-core"                % scalazVersion,
  "org.scalaz"            %% "scalaz-effect"              % scalazVersion,
  "org.scalaz"            %% "scalaz-typelevel"           % scalazVersion,
  "org.webjars"           %% "webjars-play"               % "2.2.1",
  "org.webjars"           %  "bootstrap"                  % "2.3.1",
  "mysql"                 % "mysql-connector-java"        % "5.1.29",
  jdbc,
  "com.typesafe.akka"     %% "akka-testkit"               % akkaVersion       % "test",
  "org.scalatest"         %% "scalatest"                  % "2.1.1"           % "test",
  "org.scalaz"            %% "scalaz-scalacheck-binding"  % scalazVersion     % "test"
)

seq(StandardProject.newSettings: _*)

scalacOptions += "-feature"

initialCommands in console := "import scalaz._, Scalaz._"

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
