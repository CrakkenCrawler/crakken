name := "crakken"

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

scalacOptions += "-feature"

initialCommands in console := "import scalaz._, Scalaz._"

play.Project.playScalaSettings

releaseSettings
