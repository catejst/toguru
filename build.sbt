// *** Plugins ***

lazy val root = (project in file(".")).enablePlugins(SbtWeb, PlayScala, DockerPlugin)


// *** Coordinates ***

organization := "com.autoscout24"
name         := "dimmer"
version      := Option(System.getenv("DIMMER_VERSION")).getOrElse("local")


// *** Libraries ***

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  ws,
  filters,
  "com.github.dnvriend"    %% "akka-persistence-jdbc"    % "2.6.4",
  "org.postgresql"         %  "postgresql"               % "9.4.1209",
  "net.logstash.logback"   %  "logstash-logback-encoder" % "4.7",
  "org.scalatestplus.play" %% "scalatestplus-play"       % "1.5.1"    % Test
)


// *** Runtime configurations ***

fork in run := true

javaOptions in Runtime ++= ConfigurationSettings.run

javaOptions in Test    ++= ConfigurationSettings.test


// *** Play configuration settings ***

routesGenerator := InjectedRoutesGenerator


// *** Compilation and packaging ***

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

// Don't package API docs in dist
doc in Compile <<= target.map(_ / "none")

// Remove toplevel directory containing service version
topLevelDirectory := None
