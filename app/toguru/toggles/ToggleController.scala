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

import scala.concurrent.{ExecutionContext, Future}

class ToggleController(config: Config, provider: ToggleActorProvider) extends Controller with EventPublishing {

  type ResponseMapper = PartialFunction[Any, Result]

  type FailureHandler = PartialFunction[Throwable, Result]

  @Inject
  def this(system: ActorSystem, config: Config) = this(config, ToggleActor.provider(system))

  implicit val createToggleReads = Json.reads[CreateToggleCommand]
  implicit val createToggleWrites = Json.writes[CreateToggleCommand]
  implicit val toggleWrites = Json.writes[Toggle]
  implicit val timeout = Timeout(config.actorTimeout)

  val sampleCreateToggle = CreateToggleCommand("toggle name", "toggle description", Map("team" -> "Shared Services"))

  def get(toggleId: String) = ActionWithJson.async { request =>
    import play.api.libs.concurrent.Execution.Implicits._

    withActor(toggleId, "get-toggle") { toggleActor =>
      (toggleActor ? GetToggle).map {
        case Some(toggle: Toggle) => Ok(Json.toJson(toggle))
        case None => NotFound(errorJson(
          "Not found",
          s"A toggle with id '$toggleId' does not exist",
          "Provide an existing toggle id"))
        case r => throw new RuntimeException(s"ToggleActor replied with $r")
      }
    }
  }

  def create = ActionWithJson.async(json(sampleCreateToggle)) { request =>
    import play.api.libs.concurrent.Execution.Implicits._
    val command = request.body
    val id = ToggleActor.toId(command.name)

    withActor(id, "create-toggle") { toggleActor =>
      (toggleActor ? command).map {
        case CreateSucceeded(toggleId) =>
          publisher.event("create-toggle-success", "toggle-id" -> toggleId)
          Ok(Json.obj("status" -> "Ok", "id" -> toggleId))

        case ToggleAlreadyExists(toggleId) =>
          publisher.event("create-toggle-conflict", "toggle-id" -> toggleId)
          Conflict(errorJson(
            "Conflict",
            s"A toggle with id $toggleId already exists",
            "Choose different toggle name"))

        case CreateFailed(toggleId, cause) =>
          publisher.event("create-toggle-failed", cause, "toggle-id" -> toggleId)
          InternalServerError(errorJson("Internal Server Error", cause.getMessage))
      }
    }
  }

  def withActor(toggleId: String, actionId: String)(handler: ActorRef => Future[Result])(implicit ec: ExecutionContext): Future[Result] = {
    val toggleActor = provider.create(toggleId)
    handler(toggleActor)
      .recover(serverError(s"$actionId-failed", toggleId))
      .andThen { case _ => provider.stop(toggleActor) }
  }

  def serverError(actionId: String, name: String): FailureHandler = {
    case t: Throwable =>
      publisher.event(actionId, t, "toggleId" -> ToggleActor.toId(name))
      InternalServerError(errorJson(
        "Internal server error",
        "An internal error occurred",
        "Try again later or contact service owning team"
      ))
  }
}