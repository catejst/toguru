package dimmer

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import dimmer.app.{HealthActor, JmxReportingSetup}
import play.api.libs.concurrent.AkkaGuiceSupport
import slick.backend.DatabaseConfig
import slick.driver.PostgresDriver

class DimmerModule extends AbstractModule with AkkaGuiceSupport {

  def configure() = {
    bind(classOf[Configuration]).asEagerSingleton()
    bind(classOf[JmxReportingSetup]).asEagerSingleton()

    bindActor[HealthActor]("health")
  }

  @Provides @Singleton
  def dbConfig: DatabaseConfig[PostgresDriver]= DatabaseConfig.forConfig("slick")
}
