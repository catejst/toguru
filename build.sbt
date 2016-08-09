import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale
import sbt.Keys._

organization := "com.autoscout24"

name := "dimmer"

version := Option(System.getenv("GO_PIPELINE_LABEL")).getOrElse("1.0-SNAPSHOT")
scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  ws,
  filters,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.6.4",
  "org.postgresql" % "postgresql" % "9.4.1209",
  "net.logstash.logback" % "logstash-logback-encoder" % "4.7",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

javaOptions in Test ++= Seq(
  "-Dconfig.resource=test.conf",
  "-Dlogger.resource=logger-config.xml"
)

javaOptions in Runtime ++= Seq(
  "-Dconfig.resource=application.conf",
  "-Dlogger.resource=logger-config.xml"
)

lazy val root = (project in file("."))
  .enablePlugins(SbtWeb, PlayScala)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Don't package API docs in dist
doc in Compile <<= target.map(_ / "none")

fork in run := true

// Remove toplevel directory containing service version
topLevelDirectory := None
