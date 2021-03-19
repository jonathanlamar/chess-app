package models.ai

import models.rules.UpdateGameState.{updateGameState, promotePawn}
import models.rules.ValidMoves.getLegalMoves
import models.utils.DataTypes._
import scala.util.Random

object RandomAi extends Ai {
  override def makeMove(gameState: GameState): GameState = {
    val allMoves: List[(Position, Position)] =
      gameState.piecesIndex.view
        .filterKeys(_.color == gameState.whoseMove)
        .values
        .flatten
        .flatMap(pos => getLegalMoves(gameState, pos).map((pos, _)))
        .toList

    val (srcPos, destPos) = allMoves(Random.nextInt(allMoves.length))

    val updatedGameState = updateGameState(gameState, srcPos, destPos)

    if (gameState.squares(srcPos.row)(srcPos.col) == Piece(Black, Pawn) && destPos.row == 7) {
      val promoteType = List(Knight, Rook, Bishop, Queen)(Random.nextInt(4))

      promotePawn(updatedGameState, destPos, promoteType)
    } else if (
      gameState.squares(srcPos.row)(srcPos.col) == Piece(White, Pawn) && destPos.row == 0
    ) {
      val promoteType = List(Knight, Rook, Bishop, Queen)(Random.nextInt(4))

      promotePawn(updatedGameState, destPos, promoteType)
    } else updatedGameState
  }
}
