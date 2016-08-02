package dimmer.filters

import javax.inject.Inject
import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject()(gzip: GzipFilter) extends HttpFilters {
  val filters = Seq(gzip)
}
