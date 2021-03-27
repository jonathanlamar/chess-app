package models.ai.random

import models.actions.UpdateGameState.{updateGameState, promotePawn}
import models.rules.ValidMoves.getLegalMoves
import models.utils.DataTypes._
import scala.util.Random

object RandomAi {
  def makeMove(gameState: GameState): GameState = {
    val allMoves: List[(Position, Position)] =
      gameState.piecesIndex.view
        .filterKeys(_.color == gameState.whoseMove)
        .values
        .flatten
        .flatMap(pos => getLegalMoves(gameState, pos).map((pos, _)))
        .toList

    if (allMoves.isEmpty) gameState
    else {
      val (srcPos, destPos) = allMoves(Random.nextInt(allMoves.length))

      // This will not be used if the conditions are not right for a pawn promotion.
      val promotePawnPieceType = List(Knight, Bishop, Rook, Queen)(Random.nextInt(4))

      updateGameState(gameState, srcPos, destPos, promotePawnPieceType)
    }
  }
}
