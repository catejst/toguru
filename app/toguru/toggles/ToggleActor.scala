package toguru.toggles

import akka.persistence.PersistentActor
import toguru.logging.EventPublishing
import akka.actor.{ActorRef, ActorSystem, Props}
import ToggleActor._

trait ToggleActorProvider {
  def create(id: String): ActorRef
  def stop(ref: ActorRef)
}

object ToggleActor {
  case class CreateToggleCommand(name: String, description: String, tags: Map[String, String])

  case class CreateGlobalRolloutConditionCommand(percentage: Int)

  case class UpdateGlobalRolloutConditionCommand(percentage: Int)

  case class CreateSucceeded(id: String)

  case class PersistFailed(id: String, cause: Throwable)

  case class ToggleAlreadyExists(id: String)

  case class ToggleDoesNotExist(id: String)

  case class GlobalRolloutDoesNotExist(id: String)

  case object Success

  case object GetToggle

  def toId(name: String): String = name.trim.toLowerCase.replaceAll("\\s+", "-")

  def provider(system: ActorSystem) = new ToggleActorProvider {

    def create(id: String): ActorRef = system.actorOf(Props(new ToggleActor(id)))

    def stop(ref: ActorRef): Unit = system.stop(ref)
  }
}

class ToggleActor(toggleId: String, var toggle: Option[Toggle] = None) extends PersistentActor with EventPublishing {

  val persistenceId = toggleId

  override def receiveRecover = {
    case ToggleCreated(name, description, tags) =>
      toggle = Some(Toggle(toggleId, name, description, tags))

    case GlobalRolloutCreated(percentage) =>
      toggle = toggle.map{ t => t.copy(rolloutPercentage = Some(percentage))}

    case GlobalRolloutUpdated(percentage) =>
      toggle = toggle.map{ t => t.copy(rolloutPercentage = Some(percentage))}
  }

  override def receiveCommand = handleToggleCommands.orElse(handleGlobalRolloutCommands)

  def handleToggleCommands: Receive = {
    case CreateToggleCommand(name, description, tags) =>
      toggle match {
        case Some(_) => sender ! ToggleAlreadyExists(toggleId)
        case None    => persistCreateEvent(name, description, tags)
      }

    case GetToggle => sender ! toggle
  }

  def handleGlobalRolloutCommands: Receive = withExistingToggle {
    t => {
      case CreateGlobalRolloutConditionCommand(percentage) =>
        persist(GlobalRolloutCreated(percentage)) { set =>
          receiveRecover(set)
          sender ! Success
        }

      case UpdateGlobalRolloutConditionCommand(percentage) => t.rolloutPercentage match {
        case Some(p) => persist(GlobalRolloutUpdated(percentage)) { set =>
          receiveRecover(set)
          sender ! Success
        }

        case None => sender ! GlobalRolloutDoesNotExist(toggleId)
      }
    }
  }

  def persistCreateEvent(name: String, description: String, tags: Map[String, String]): Unit = {
    persist(ToggleCreated(name, description, tags)) { created =>
      receiveRecover(created)
      sender ! CreateSucceeded(toggleId)
    }
  }

  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    sender ! PersistFailed(toggleId, cause)
  }

  def withExistingToggle(handler: Toggle => Receive): Receive = {
    case command if toggle.isDefined => handler(toggle.get)(command)
    case _                           => sender ! ToggleDoesNotExist(toggleId)
  }
}
