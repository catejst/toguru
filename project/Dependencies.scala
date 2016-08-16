import sbt._

object Version {

  val ScalaPb         = "0.5.34"
  val Play            = "2.5.4"
  val NativePackager  = "1.2.0-M5"
  val AkkaPersistence = "2.6.4"
  val Postgres        = "9.4.1209"
  val LogstashEncoder = "4.7"
  val ScalaTestPlus   = "1.5.1"

}

object Library {
  val AkkaPersistence = "com.github.dnvriend"    %% "akka-persistence-jdbc"    % Version.AkkaPersistence
  val Postgres        = "org.postgresql"         %  "postgresql"               % Version.Postgres
  val LogstashEncoder = "net.logstash.logback"   %  "logstash-logback-encoder" % Version.LogstashEncoder
  val ScalaPbRuntime  = "com.trueaccord.scalapb" %% "scalapb-runtime"          % Version.ScalaPb
  val ScalaTestPlus   = "org.scalatestplus.play" %% "scalatestplus-play"       % Version.ScalaTestPlus
}
