package dimmer.app

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import play.api.libs.concurrent.AkkaGuiceSupport
import slick.backend.DatabaseConfig
import slick.driver.JdbcDriver

class DimmerModule extends AbstractModule with AkkaGuiceSupport {

  def configure() = {
    bind(classOf[JmxReportingSetup]).asEagerSingleton()
    bind(classOf[Config]).to(classOf[Configuration]).asEagerSingleton()

    bindActor[HealthActor]("health")
  }

  @Provides @Singleton
  def dbConfig: DatabaseConfig[JdbcDriver] = DatabaseConfig.forConfig("slick")
}
