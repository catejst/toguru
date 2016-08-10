import com.typesafe.sbt.packager.docker._

// *** Dockerfile settings ***

// Using openjdk base image due to licensing issues of Oracle Java.
dockerBaseImage    := "java:openjdk-8-jre-alpine"

dockerExposedPorts := Seq(9000)

dockerCmd          := ConfigurationSettings.run

// installing Bash for native packager run script
val installBash = Cmd("RUN", "apk add --update bash && rm -rf /var/cache/apk/*")

val healthCheck = Cmd("HEALTHCHECK", "--interval=5s --timeout=3s --retries=3 " +
  "CMD wget -nv http://localhost:9000/healthcheck || exit 1")

// add bash install and health check to standard Dockerfile commands
dockerCommands := dockerCommands.value.head +:
                  installBash +:
                  healthCheck +:
                  dockerCommands.value.tail


// *** Docker publishing settings ***

dockerUpdateLatest := false

dockerRepository   := Some("as24")

version in Docker  := version.value
