package dimmer.toggles

import akka.actor.Props
import dimmer.DbSetup
import dimmer.app.Config
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.ws.WSClient
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import scala.concurrent.Await
import scala.concurrent.duration._


class ToggleIntegrationSpec extends PlaySpec with BeforeAndAfterEach  with DbSetup with OneServerPerSuite with FutureAwaits with DefaultAwaitTimeout {
  override def log(message: String): Unit = info(message)

  override protected def beforeEach(): Unit = startPostgres()

  override protected def afterEach(): Unit = stopPostgres()

  "ToggleEndpoint" should {
    "successfully create toggles" in {
      info("waiting for postgres")
      waitForPostgres()
      info("postgres ready")
      val wsClient = app.injector.instanceOf[WSClient]
      val toggleEndpointURL = s"http://localhost:$port/toggle"
      val body = """{"name" : "toggle name","description" : "toggle description","tags" : {"team" : "Shared Services"}}"""
      await(wsClient.url(toggleEndpointURL).post("""{"name" : "toggle name 2","description" : "toggle description","tags" : {"team" : "Shared Services"}}"""))
      val response = await(wsClient.url(toggleEndpointURL).post(body))
      response.status mustBe 200


//      val actor = app.actorSystem.actorOf(Props(new ToggleActor("myToggle")))
//      implicit val timeout = Timeout(15000.millis)
//      val response = Await.result(actor ? CreateToggleCommand("myToggle", "description", Map("team" -> "Shared Services")), 15500.millis)
//      response mustBe CreateSucceeded

    }
  }



  override def config = app.injector.instanceOf[Config].typesafeConfig
}
