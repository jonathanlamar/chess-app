package controllers

import java.net.URLDecoder
import javax.inject._
import models.rules.Check.getCurrentPlayerCheckStatus
import models.utils.DataTypes._
import play.api._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class CheckController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  implicit val checkWrites: Writes[CheckStatus] = (JsPath \ "check")
    .write[Boolean]
    .and((JsPath \ "checkmate").write[Boolean])(
      unlift(CheckStatus.unapply)
    )

  def getAll(fenString: String): Action[AnyContent] = Action {
    val gameState = GameState(URLDecoder.decode(fenString))

    // TODO: Try logic for exception handling
    val json = Json.toJson(getCurrentPlayerCheckStatus(gameState))

    Ok(json)
  }
}
