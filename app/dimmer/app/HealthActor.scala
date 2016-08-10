package dimmer.app

import java.util.concurrent.TimeoutException
import javax.inject.Inject

import akka.actor.Actor
import akka.pattern.after
import dimmer.app.HealthActor.{CheckHealth, GetHealth, HealthStatus}
import slick.backend.DatabaseConfig
import slick.driver.PostgresDriver

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

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
    import dbConfig.driver.api._
    implicit val ec: ExecutionContext = context.system.dispatcher

    def timeout[T](future: Future[T]): Future[T] = {
      val timeoutFuture = after(500.milliseconds, context.system.scheduler) { Future.failed(new TimeoutException()) }
      Future.firstCompletedOf(List(future, timeoutFuture))
    }

    timeout(dbConfig.db.run(sql"""SELECT 1""".as[Int].head))
      .map(result => databaseHealthy = result == 1)
      .recover { case _ => databaseHealthy = false }
  }

  override def receive = {
    case CheckHealth() => checkHealth()
    case GetHealth() => sender ! HealthStatus(databaseHealthy)
  }
}