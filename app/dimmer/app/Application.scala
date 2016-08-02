package dimmer.app

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok("Your new application is ready.")
  }

  def healthCheck = Action(Ok("Ok"))

}