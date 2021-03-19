package controllers

import java.net.URLDecoder
import javax.inject._
import models.rules.Check.isPlayerInCheck
import models.rules.UpdateGameState.updateGameState
import models.rules.ValidMoves.allPossibleMoves
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

  implicit val positionWrites: Writes[Position] = Writes[Position](p => JsString(p.toFileRank()))

  def getAll(
      fenString: String,
      movingPieceFileRank: String,
      isInCheck: Boolean
  ): Action[AnyContent] = Action {
    val gameState = GameState(URLDecoder.decode(fenString))
    val movingPiecePos = Position(movingPieceFileRank)

    val pseudoLegalMoves = allPossibleMoves(gameState, movingPiecePos)

    val legalMoves = pseudoLegalMoves.filter(newPos =>
      !isPlayerInCheck(updateGameState(gameState, movingPiecePos, newPos), gameState.whoseMove)
    )

    val json = Json.toJson(legalMoves)
    // TODO: Try logic for exception handling
    Ok(json)
  }
}
