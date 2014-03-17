// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

//for sbt-release
resolvers += "SBT Plugins" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"

//for sbt-bintray
resolvers += "bintray-sbt-plugin-releases" at "http://dl.bintray.com/content/sbt/sbt-plugin-releases"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.2")

//Handles version numbers
addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.2")

//Handles publishing to bintray
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

//Generates BuildInfo so that versioning information can be consumed in code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.1")