package dimmer.app

import javax.inject.Inject

import akka.actor.Actor
import dimmer.app.HealthActor.{CheckHealth, GetHealth, HealthStatus}
import slick.backend.DatabaseConfig
import slick.driver.PostgresDriver

import scala.concurrent.Await
import scala.concurrent.duration._

object HealthActor {

  case class CheckHealth()
  case class GetHealth()
  case class HealthStatus(isDatabaseHealthy: Boolean)
}


class HealthActor @Inject() (dbConfig: DatabaseConfig[PostgresDriver]) extends Actor  {

  var databaseHealthy: Boolean = false

  val repeatedlyHealthCheck = {
    implicit val ec = context.system.dispatcher

    context.system.scheduler.schedule(0.seconds, 1.second, self, CheckHealth())
  }

  override def postStop() = repeatedlyHealthCheck.cancel()

  def checkHealth(): Unit = {

    val db = dbConfig.db
    try {

      import dbConfig.driver.api._

      val sqlStatement = sql"""SELECT 1""".as[Int].head
      val result: Int = Await.result(db.run(sqlStatement), 500.milliseconds)

      databaseHealthy = result == 1
    } catch {
      case e: Exception => {
        databaseHealthy = false
      }
    }
  }

  override def receive = {
    case CheckHealth() => checkHealth()
    case GetHealth() => sender ! HealthStatus(databaseHealthy)
  }
}