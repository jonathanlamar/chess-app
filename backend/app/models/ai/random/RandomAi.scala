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
      val updatedGameState = updateGameState(gameState, srcPos, destPos)
      val srcPiece = gameState.squares(srcPos.row)(srcPos.col)

      srcPiece match {
        case Blank    => throw new Exception("Source piece is blank")
        case p: Piece => maybeHandlePawnPromotion(updatedGameState, p, destPos)
      }
    }
  }

  def maybeHandlePawnPromotion(
      gameState: GameState,
      srcPiece: Piece,
      destPos: Position
  ): GameState = {
    val promoteType = List(Knight, Rook, Bishop, Queen)(Random.nextInt(4))

    if (srcPiece == Piece(Black, Pawn) && destPos.row == 7) {
      promotePawn(gameState, destPos, promoteType)
    } else if (srcPiece == Piece(White, Pawn) && destPos.row == 0) {
      promotePawn(gameState, destPos, promoteType)
    } else gameState
  }
}
