package dimmer.toggles

import akka.persistence.PersistentActor
import dimmer.logging.EventPublishing

case class Toggle(name: String, description: String, tags: Map[String, String])

class ToggleActor(name: String) extends PersistentActor with EventPublishing {

  val persistenceId = name

  var toggle: Toggle = Toggle(name, "", Map.empty)

  override def receiveRecover: Receive = {
    case ToggleCreated(name, description, tags) =>
      toggle = Toggle(name, description, tags)
  }

  override def receiveCommand: Receive = {
    case CreateToggleCommand(name, description, tags) =>
      persist(ToggleCreated(name, description, tags)) { created =>
        receiveRecover(created)
        sender ! CreateSucceeded
      }
    case GetToggle => sender ! toggle
  }

  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    sender ! CreateFailed(cause.getMessage)
    publisher.event("toggle-event-persist-failed",cause,"event_type" -> event.getClass.getSimpleName)
  }
}

