package dimmer.toggles

import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

object RestUtils {

  def badHeaderResponse(request: Request[_], allowedHeaders: String*) = {
    import HeaderNames.ACCEPT
    import Results.BadRequest

    val givenAcceptHeaders = request.acceptedTypes.mkString(", ")
    BadRequest(Json.obj(
      "status" -> "Bad Request",
      "reason" -> s"No supported $ACCEPT request header provided (given was '$givenAcceptHeaders').",
      "remedy" -> s"Include ${allowedHeaders.mkString(", or")} in the $ACCEPT request header"))
  }

  object OnlyJson extends ActionFilter[Request] with HeaderNames with AcceptExtractors with Results {

    override protected def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful {
      request match {
        case Accepts.Json() => None
        case _ => Some(badHeaderResponse(request, MimeTypes.JSON))
      }
    }
  }


  val ActionWithJson = Action andThen OnlyJson
}