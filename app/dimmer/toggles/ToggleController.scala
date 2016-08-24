package dimmer.toggles

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import RestUtils._
import dimmer.app.Config
import play.api.http.ContentTypes
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import ToggleActor._
import dimmer.logging.EventPublishing

trait ToggleActorProvider {
  def create(name: String): ActorRef
  def stop(ref: ActorRef)
}

class ToggleController(config: Config, factory: ToggleActorProvider) extends Controller with EventPublishing {

  @Inject
  def this(system: ActorSystem, config: Config) = this(config, provider(system))

  implicit val createToggleReads = Json.reads[CreateToggleCommand]
  implicit val createToggleWrites = Json.writes[CreateToggleCommand]

  val sampleCreateToggle = CreateToggleCommand("toggle name", "toggle description", Map("team" -> "Shared Services"))

  def create = ActionWithJson.async(parse.tolerantJson) { request =>
    request.body.validate[CreateToggleCommand] match {
      case e: JsError        => invalidRequestBody(e)
      case JsSuccess(cmd, _) => handleCreate(cmd)
    }
  }

  def handleCreate(cmd: CreateToggleCommand): Future[Result] = {
    val toggleActor = factory.create(cmd.name)
    implicit val timeout = Timeout(config.actorTimeout)
    import play.api.libs.concurrent.Execution.Implicits._
    (toggleActor ? cmd)
      .map(toResponse)
      .recover(serverError(cmd.name))
      .andThen {
        case _ => factory.stop(toggleActor)
      }
  }

  def invalidRequestBody(error: JsError): Future[Result] = {
    val errorsObj = JsObject(error.errors.flatMap {
      case (path, errors) => errors.map(e => path.toString() -> JsString(e.message))
    })

    Future.successful(BadRequest(Json.prettyPrint(Json.obj(
      "status" -> "Bad Request",
      "reason" -> "Provided Body not valid",
      "remedy" -> "Provide valid Body of the form given in the sample field",
      "sample" -> Json.toJson(sampleCreateToggle),
      "errors" -> errorsObj
    ))).as(ContentTypes.JSON))
  }

  val toResponse: PartialFunction[Any, Result] = {
    case CreateSucceeded(toggleId) =>
      publisher.event("create-toggle-success", "toggle-id" -> toggleId)
      Ok(Json.obj("status" -> "Ok", "id" -> toggleId))

    case ToggleAlreadyExists(toggleId) =>
      publisher.event("create-toggle-conflict", "toggle-id" -> toggleId)
      Conflict(Json.obj(
        "status" -> "Conflict",
        "reason" -> s"A toggle with id $toggleId already exists",
        "remedy" -> "Choose different toggle name"))

    case CreateFailed(toggleId, cause) =>
      publisher.event("create-toggle-failed", cause, "toggle-id" -> toggleId)
      InternalServerError(Json.obj("status" -> "Internal Server Error", "reason" -> cause.getMessage))
  }

  def serverError(name: String): PartialFunction[Throwable, Result] = {
    case t: Throwable =>
      publisher.event("create-toggle-failed", t, "toggle-id" -> ToggleActor.toId(name))
      InternalServerError(Json.obj(
        "status" -> "Internal Server Error",
        "reason" -> "An internal error occurred",
        "remedy" -> "Try again later or contact service owning team"
      ))
  }
}