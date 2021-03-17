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
  implicit val colorWrites: Writes[Color] = JsPath.write[String].contramap(_.toString)
  implicit val typeWrites: Writes[PieceType] = JsPath.write[String].contramap(_.toString)
  implicit val pieceWrites: Writes[Piece] =
    (JsPath \ "color").write[Color].and((JsPath \ "type").write[PieceType])(unlift(Piece.unapply))
  implicit val boardWrites: Writes[GameState] =
    (JsPath \ "fen")
      .write[String]
      .and((JsPath \ "whiteCapturedPieces").write[List[Piece]])
      .and((JsPath \ "blackCapturedPieces").write[List[Piece]])(unlift(GameState.unapply))

  // I don't understand this syntax very well, but this is the only way I know
  // to coax the desired format.
  case class GameState(
      fenString: String,
      blackCapturedPieces: List[Piece],
      whiteCapturedPieces: List[Piece]
  )

  object GameState {
    def apply(board: Board): GameState =
      GameState(toFenString(board), board.whiteCapturedPieces, board.blackCapturedPieces)
  }

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
    val json = Json.toJson(GameState(updatedBoard))

    Ok(json)
  }
}
