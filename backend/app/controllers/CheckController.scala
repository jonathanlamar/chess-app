package controllers

import java.net.URLDecoder
import javax.inject._
import models.rules.Check.isCurrentPlayerInCheck
import models.utils.DataTypes._
import play.api._
import play.api.libs.json._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class CheckController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def getAll(fenString: String): Action[AnyContent] = Action {
    val gameState = GameState(URLDecoder.decode(fenString))

    // TODO: Try logic for exception handling
    val json = Json.toJson(isCurrentPlayerInCheck(gameState))

    Ok(json)
  }
}
