package dimmer.app

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import com.typesafe.config.{Config => TypesafeConfig}

import scala.concurrent.duration._

trait Config {
  val typesafeConfig: TypesafeConfig
  val actorTimeout: FiniteDuration
}

@Singleton
class Configuration @Inject() (playConfig: play.api.Configuration) extends Config {
  lazy val typesafeConfig = playConfig.underlying
  val actorTimeout: FiniteDuration = typesafeConfig.getDuration("actorTimeout",TimeUnit.MILLISECONDS).milliseconds
}