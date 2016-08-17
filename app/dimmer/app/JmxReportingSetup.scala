package dimmer.app

import javax.inject.Inject

import com.codahale.metrics.JmxReporter
import com.kenshoo.play.metrics.Metrics

class JmxReportingSetup @Inject()(metrics: Metrics) {
  val reporter = JmxReporter.forRegistry(metrics.defaultRegistry).build()
  reporter.start()
}