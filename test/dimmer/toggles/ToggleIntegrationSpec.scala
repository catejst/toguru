package dimmer.toggles

import dimmer.PostgresSetup
import dimmer.app.Config
import play.api.test.Helpers._
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Results
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}


class ToggleIntegrationSpec extends PlaySpec with BeforeAndAfterAll with Results with PostgresSetup with OneServerPerSuite with FutureAwaits with DefaultAwaitTimeout {
  override def log(message: String): Unit = info(message)

  override protected def beforeAll(): Unit = startPostgres()

  override protected def afterAll(): Unit = stopPostgres()

  "ToggleEndpoint" should {
    "successfully create toggles" in {
      waitForPostgres()
      val wsClient = app.injector.instanceOf[WSClient]
      val toggleEndpointURL = s"http://localhost:$port/toggle"
      val body = """{"name" : "toggle name","description" : "toggle description","tags" : {"team" : "Shared Services"}}"""
      val response = await(wsClient.url(toggleEndpointURL).post(body))
      response.status mustBe OK
      val json = Json.parse(response.body)

      (json \ "status").asOpt[String] mustBe Some("OK")
    }
  }



  override def config = app.injector.instanceOf[Config].typesafeConfig
}
