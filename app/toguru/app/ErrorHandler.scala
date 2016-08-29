package toguru.app

import javax.inject._

import toguru.logging.EventPublishing
import play.api.{Environment, OptionalSourceMapper, UsefulException, Configuration => PlayConfiguration}
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.libs.json.Json

class ErrorHandler @Inject()(
                      env: Environment,
                      config: PlayConfiguration,
                      sourceMapper: OptionalSourceMapper,
                      router: Provider[Router]
                    ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with EventPublishing with AcceptExtractors {

  def reply(request: RequestHeader, status: Status, statusText: String, reason: String, superRespone: => Future[Result]) = {
    def json =
      if (reason.trim.isEmpty)
        Future.successful(status(Json.obj("status" -> statusText)))
      else
        Future.successful(status(Json.obj("status" -> statusText, "reason" -> reason)))

    request match {
      case Accepts.Json() => json
      case _              => superRespone
    }
  }

  override protected def onBadRequest(request: RequestHeader, message: String) =
    reply(request, BadRequest, "Bad request", message, super.onBadRequest(request, message))

  override protected def onForbidden(request: RequestHeader, message: String) =
    reply(request, Forbidden, "Forbidden", message, super.onForbidden(request, message))

  override protected def onNotFound(request: RequestHeader, message: String) =
    reply(request, NotFound, "Not found", message, super.onNotFound(request, message))

  override protected def onOtherClientError(request: RequestHeader, statusCode: Int, message: String) =
    reply(request, Status(statusCode), "Client error", message, super.onOtherClientError(request, statusCode, message))

  override protected def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    reply(request, InternalServerError, "Internal server error", "An internal error occurred. Sorry for that.",
      super.onProdServerError(request, exception))
  }
}
