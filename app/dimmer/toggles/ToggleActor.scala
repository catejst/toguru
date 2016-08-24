package dimmer.toggles

import akka.persistence.PersistentActor
import dimmer.logging.EventPublishing
import akka.actor.{ActorRef, ActorSystem, Props}

import ToggleActor._

case class Toggle(name: String, description: String, tags: Map[String, String])

object ToggleActor {
  case class CreateToggleCommand(name: String, description: String, tags: Map[String, String])

  case class CreateSucceeded(id: String)

  case class CreateFailed(id: String, cause: Throwable)

  case class ToggleAlreadyExists(id: String)

  case object GetToggle

  def toId(name: String): String = name.trim.toLowerCase.replaceAll("\\s+", "-")

  def provider(system: ActorSystem) = new ToggleActorProvider {

    def create(name: String): ActorRef = system.actorOf(Props(new ToggleActor(toId(name))))

    def stop(ref: ActorRef): Unit = system.stop(ref)
  }
}

class ToggleActor(toggleId: String) extends PersistentActor with EventPublishing {

  val persistenceId = toggleId

  var toggle: Option[Toggle] = None

  override def receiveRecover: Receive = {
    case ToggleCreated(name, description, tags) =>
      toggle = Some(Toggle(name, description, tags))
  }

  override def receiveCommand: Receive = {
    case CreateToggleCommand(name, description, tags) =>
      toggle match {
        case Some(_) => sender ! ToggleAlreadyExists(toggleId)
        case None    => persistCreateEvent(name, description, tags)
      }
    case GetToggle => sender ! toggle
  }

  def persistCreateEvent(name: String, description: String, tags: Map[String, String]): Unit = {
    persist(ToggleCreated(name, description, tags)) { created =>
      receiveRecover(created)
      sender ! CreateSucceeded(toggleId)
    }
  }

  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    sender ! CreateFailed(toggleId, cause)
  }
}
