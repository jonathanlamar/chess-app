package models.rules

import models.rules.ValidMoves.allPossibleMoves
import models.utils.DataTypes._

object Check {
  def getAttackSquares(gameState: GameState, color: Color): List[Position] = {
    val attackingPiecePositions =
      gameState.piecesIndex
        .filter({ case (k: Square, v: List[Position]) => k.color == color })
        .values
        .toList
        .flatten

    attackingPiecePositions
      .flatMap(pos => allPossibleMoves(gameState.updateWhoseMove(color), pos))
      .distinct
  }

  def isCurrentPlayerInCheck(gameState: GameState): Boolean = {
    val attackSquares =
      if (gameState.whoseMove == White) getAttackSquares(gameState, Black)
      else getAttackSquares(gameState, White)

    // TODO: Good use for Try block
    gameState.piecesIndex.get(Piece(gameState.whoseMove, King)) match {
      case None => throw new Exception("No king key in index")
      case Some(posList) =>
        posList.headOption match {
          case None      => throw new Exception("No king value in index")
          case Some(pos) => attackSquares.contains(pos)
        }
    }
  }
}
