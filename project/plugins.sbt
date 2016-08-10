resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.sbtPluginRepo("releases")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M5")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")
