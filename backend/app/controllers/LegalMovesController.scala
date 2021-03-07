package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class LegalMovesController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def getAll(fenString: String, movingPieceRankFile: String): Action[AnyContent] = Action {
    NoContent
  }
}
