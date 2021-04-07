package controllers

import java.net.URLDecoder
import javax.inject._
import models.ai.minimax.AlphaBeta
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
class AiController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  implicit val pieceWrites: Writes[Piece] = Writes[Piece](p => JsString(p.toString()))
  implicit val boardWrites: Writes[JsonFriendlyGameState] =
    (JsPath \ "fen")
      .write[String]
      .and((JsPath \ "whiteCapturedPieces").write[List[Piece]])
      .and((JsPath \ "blackCapturedPieces").write[List[Piece]])(
        unlift(JsonFriendlyGameState.unapply)
      )
  val alphaBeta = new AlphaBeta()

  // I don't understand this syntax very well, but this is the only way I know
  // to coax the desired format.
  case class JsonFriendlyGameState(
      fenString: String,
      blackCapturedPieces: List[Piece],
      whiteCapturedPieces: List[Piece]
  )

  object JsonFriendlyGameState {
    def apply(gameState: GameState): JsonFriendlyGameState =
      JsonFriendlyGameState(
        toFenString(gameState),
        gameState.blackCapturedPieces,
        gameState.whiteCapturedPieces
      )
  }

  def getRandom(fenString: String): Action[AnyContent] = Action {
    val gameState = GameState(URLDecoder.decode(fenString))

    val updatedBoard: GameState = alphaBeta.makeMove(gameState)

    // TODO: Try logic for exception handling
    val json = Json.toJson(JsonFriendlyGameState(updatedBoard))

    Ok(json)
  }
}
