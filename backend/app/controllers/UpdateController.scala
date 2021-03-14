package controllers

import java.net.URLDecoder
import javax.inject._
import models.rules.UpdateGameState._
import models.utils.DataTypes._
import models.utils.Fen._
import play.api._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

/** This controller creates an `Action` to handle HTTP requests to the valid
  * moves generator.
  */
@Singleton
class UpdateController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  // TODO: WTF is going on here.
  implicit val posWrites: Writes[Position] =
    (JsPath \ "r").write[Int].and((JsPath \ "c").write[Int])(unlift(Position.unapply))

  def getAll(
      fenString: String,
      movingPieceFileRank: String,
      destinationFileRank: String
  ): Action[AnyContent] = Action {
    val board = Board(URLDecoder.decode(fenString))
    val movingPiecePos = Position(movingPieceFileRank)
    val destinationPos = Position(destinationFileRank)
    val updatedBoard: Board = updateGameState(board, movingPiecePos, destinationPos)

    // TODO: Try logic for exception handling
    Ok(toFenString(updatedBoard))
  }
}
