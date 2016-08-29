package toguru.toggles

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import RestUtils._
import toguru.app.Config
import play.api.libs.json._
import play.api.mvc._

import ToggleActor._
import toguru.logging.EventPublishing

trait ToggleActorProvider {
  def create(name: String): ActorRef
  def stop(ref: ActorRef)
}

class ToggleController(config: Config, factory: ToggleActorProvider) extends Controller with EventPublishing {

  @Inject
  def this(system: ActorSystem, config: Config) = this(config, provider(system))

  implicit val createToggleReads = Json.reads[CreateToggleCommand]
  implicit val createToggleWrites = Json.writes[CreateToggleCommand]
  implicit val timeout = Timeout(config.actorTimeout)

  val sampleCreateToggleJson =
    Json.toJson(CreateToggleCommand("toggle name", "toggle description", Map("team" -> "Shared Services")))

  def create = ActionWithJson.async(json[CreateToggleCommand](sampleCreateToggleJson)) { request =>
    val command = request.body
    val toggleActor = factory.create(command.name)
    import play.api.libs.concurrent.Execution.Implicits._
    (toggleActor ? command)
      .map(toResponse)
      .recover(serverError(command.name))
      .andThen {
        case _ => factory.stop(toggleActor)
      }
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
        "status" -> "Internal server error",
        "reason" -> "An internal error occurred",
        "remedy" -> "Try again later or contact service owning team"
      ))
  }
}