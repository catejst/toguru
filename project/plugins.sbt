resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.sbtPluginRepo("releases")
//resolvers += Resolver.url(
//  "sbt-plugin-releases on bintray",
//  new URL("https://dl.bintray.com/sbt/sbt-plugin-releases/")
//)(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager" % "1.0.6")