package dimmer.toggles

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import RestUtils._
import dimmer.app.Config
import play.api.http.ContentTypes
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._

trait ToggleActorFactory {
  def create(name: String): ActorRef
  def stop(ref: ActorRef)
}

class ToggleController(config: Config, factory: ToggleActorFactory) extends Controller {

  @Inject
  def this(system: ActorSystem, config: Config) = this(config, new ToggleActorFactory {

    def create(name: String): ActorRef = system.actorOf(Props(new ToggleActor(name)))

    def stop(ref: ActorRef): Unit = system.stop(ref)
  })

  implicit val createToggleReads = Json.reads[CreateToggleCommand]
  implicit val createToggleWrites = Json.writes[CreateToggleCommand]


  val sampleCreateToggle = CreateToggleCommand("toggle name", "toggle description", Map("team" -> "Shared Services"))

  def create = ActionWithJson.async(parse.tolerantJson) { request =>
    request.body.validate[CreateToggleCommand] match {
      case e: JsError => Future.successful(BadRequest(Json.prettyPrint(Json.obj(
        "status" -> "Bad Request",
        "reason" -> "Provided Body not valid",
        "remedy" -> "Provide valid Body of the form given in the sample field",
        "sample" -> Json.toJson(sampleCreateToggle)))).as(ContentTypes.JSON))

      case JsSuccess(cmd, _) =>
        val toggleActor = factory.create(cmd.name)
        implicit val timeout = Timeout(config.actorTimeout)
        val cmdResponse: Future[Any] = toggleActor ? cmd
        import play.api.libs.concurrent.Execution.Implicits._
        cmdResponse
          .map {
            case CreateSucceeded => Ok(Json.obj("status" -> "OK"))
            case CreateFailed(cause) => InternalServerError(Json.obj("status" -> "Internal Server Error", "reason" -> cause))
          }.recover {
            case _ => InternalServerError(Json.obj("status" -> "Internal Server Error", "reason" -> "Request timed out"))
          }.andThen {
            case _ => factory.stop(toggleActor)
          }
    }
  }
}

case class CreateToggleCommand(name: String, description: String, tags: Map[String, String])

case object CreateSucceeded

case class CreateFailed(cause: String)

case object GetToggle
