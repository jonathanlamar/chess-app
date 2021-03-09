package controllers

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
    (JsPath \ "row").write[Int].and((JsPath \ "col").write[Int])(unlift(Position.unapply))

  // TODO: URL encoding/decoding for fen string
  def getAll(fenString: String, movingPieceFileRank: String): Action[AnyContent] = Action {
    // val board = Board(fenString)
    val movingPiecePos = Position(movingPieceFileRank)

    val json = Json.toJson(allPossibleMovesDumb(movingPiecePos))
    // TODO: Try logic for exception handling
    Ok(json)
  }
}
