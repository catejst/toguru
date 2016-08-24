package dimmer.app

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import dimmer.app.HealthActor.{GetHealth, HealthStatus}
import play.api.mvc._

import scala.concurrent.duration._

class Application @Inject() (@Named("health") healthActor: ActorRef) extends Controller {

  def index = Action {
    Ok("Your new application is ready.")
  }

  def healthCheck = Action.async {

    import play.api.libs.concurrent.Execution.Implicits._

    implicit val timeout = Timeout(500.milliseconds)
    (healthActor ? GetHealth())
      .mapTo[HealthStatus]
      .map(toResponse)
      .recover(serverError)
  }

  private def toResponse(health: HealthStatus): Result =
    if (health.isDatabaseHealthy) Ok("Ok") else InternalServerError("Database not available")

  private val serverError: PartialFunction[Throwable, Result] = { case _ => InternalServerError("Service is not available") }

}
