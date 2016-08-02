package dimmer

import com.google.inject.AbstractModule

class DimmerModule extends AbstractModule {

  def configure() = {
    bind(classOf[Configuration]).asEagerSingleton()
  }
}
