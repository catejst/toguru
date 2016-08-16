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

import Library._
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

libraryDependencies ++= Seq(
  ws,
  filters,
  AkkaPersistence,
  Postgres,
  LogstashEncoder,
  ScalaPbRuntime  % PB.protobufConfig,
  ScalaTestPlus   % Test
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


// *** Protobuf settings ***

// load default settings.
PB.protobufSettings

// look for .proto files in /conf/protobuf
sourceDirectory in PB.protobufConfig := baseDirectory.value / "conf" / "protobuf"

// add generated source files to Scala source path
scalaSource in PB.protobufConfig <<= (sourceManaged in Compile)

// use Protocol Buffers version 3
version in PB.protobufConfig := "3.0.0-beta-3"
PB.runProtoc in PB.protobufConfig := (args => com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))
