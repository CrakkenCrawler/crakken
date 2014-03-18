// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Twitter" at "http://maven.twttr.com/"

resolvers += "Bintray" at "http://dl.bintray.com/crakkencrawler/twitter-sbt-package-dist"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.2")

//Handles version numbers
addSbtPlugin("com.twitter" % "sbt-package-dist" % "1.1.0")

//Generates BuildInfo so that versioning information can be consumed in code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.1")