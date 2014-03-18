import com.twitter.sbt._

name := "crakken"

organization := "crakkencrawler"

version := "0.1.8-SNAPSHOT"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-actor"     % "2.2.4",
  "com.typesafe.akka"     %% "akka-remote"    % "2.2.4",
  "com.typesafe.akka"     %% "akka-slf4j"     % "2.2.4",
  "com.typesafe.play"     %% "play-slick"     % "0.6.0.1",
  "io.spray"              %  "spray-client"   % "1.2.0",
  "io.spray"              %  "spray-caching"  % "1.2.0",
  "org.jsoup"             %  "jsoup"          % "1.7.3",
  "org.webjars"           %% "webjars-play"   % "2.2.1",
  "org.webjars"           %  "bootstrap"      % "2.3.1",
  jdbc,
  "com.typesafe.akka"     %% "akka-testkit"   % "2.2.1"           % "test",
  "org.scalatest"         %% "scalatest"      % "2.1.0-RC3"       % "test"
)

val scalazVersion = "7.0.5"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-effect" % scalazVersion,
  "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
  "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test"
)

play.Project.playScalaSettings

scalacOptions += "-feature"

initialCommands in console := "import scalaz._, Scalaz._"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

seq(StandardProject.newSettings: _*)

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
