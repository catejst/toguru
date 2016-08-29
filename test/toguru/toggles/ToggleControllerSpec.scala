package toguru.toggles

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import toguru.app.Config
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.duration._
import com.typesafe.config.{Config => TypesafeConfig}
import toguru.toggles.ToggleActor.{CreateSucceeded, CreateToggleCommand}

class ToggleControllerSpec extends PlaySpec with Results with MockitoSugar {

  def createController(props: Props = Props[MockToggleActor]): ToggleController = {
    val system = ActorSystem()
    val factory = new ToggleActorProvider {


      def create(name: String): ActorRef = system.actorOf(props)

      def stop(ref: ActorRef): Unit = system.stop(ref)
    }
    val config = new Config {
      val typesafeConfig = mock[TypesafeConfig]
      val actorTimeout: FiniteDuration = 10.millis
    }

    new ToggleController(config, factory)
  }

  "returns ok when json is valid" in {

    val controller = createController()
    val request = FakeRequest().withBody(CreateToggleCommand("toggle", "description", Map.empty))

    val result: Future[Result] = controller.create().apply(request)

    val bodyJson: JsValue = contentAsJson(result)
    status(result) mustBe 200
    (bodyJson \ "status").asOpt[String] mustBe Some("Ok")
  }

  "returns 500 when actor request times out" in {

    val controller = createController(Props[MockLazyActor])
    val request = FakeRequest().withBody(CreateToggleCommand("toggle", "description", Map.empty))

    val result: Future[Result] = controller.create().apply(request)

    val bodyJson: JsValue = contentAsJson(result)
    status(result) mustBe 500
    (bodyJson \ "status").asOpt[String] mustBe Some("Internal server error")
    (bodyJson \ "reason").asOpt[String] mustBe Some("An internal error occurred")
  }
}

class MockToggleActor extends Actor {
  override def receive: Receive = {
    case _ => sender ! CreateSucceeded("toggle-id")
  }
}

class MockLazyActor extends Actor {
  override def receive: Receive = {
    case _ => ()
  }
}