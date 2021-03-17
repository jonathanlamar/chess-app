package controllers

import java.net.URLDecoder
import javax.inject._
import models.rules.ValidMoves._
import models.utils.DataTypes._
import play.api._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class LegalMovesController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  // TODO: WTF is going on here.
  implicit val posWrites: Writes[Position] =
    (JsPath \ "r").write[Int].and((JsPath \ "c").write[Int])(unlift(Position.unapply))

  def getAll(fenString: String, movingPieceFileRank: String): Action[AnyContent] = Action {
    val gameState = GameState(URLDecoder.decode(fenString))
    val movingPiecePos = Position(movingPieceFileRank)

    val json = Json.toJson(allPossibleMoves(gameState, movingPiecePos))
    // TODO: Try logic for exception handling
    Ok(json)
  }
}
