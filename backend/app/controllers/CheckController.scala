package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class CheckController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  def getAll(fenString: String): Action[AnyContent] = Action {
    NoContent
  }
}
