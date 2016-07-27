package dimmer

import javax.inject.{Inject, Singleton}

@Singleton
class Configuration @Inject() (playConfig: play.api.Configuration) {
  lazy val config = playConfig.underlying
}