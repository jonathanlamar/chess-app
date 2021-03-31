package models.ai.minimax

import models.actions.UpdateGameState.updateGameState
import models.ai.minimax.Scoring.score
import models.rules.Check.isCurrentPlayerInCheck
import models.rules.ValidMoves._
import models.utils.DataTypes._
import scala.collection.parallel.ParSeq
import scala.math.max

object AlphaBeta {
  val negativeInfinity = Int.MinValue

  /** Minmax algorithm with alpha/beta pruning. See
    * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
    *
    * @param gameState - current state of play
    * @param depth - max depth of tree to search, where 0 means score current game
    * @param alpha
    * @param beta
    * @return best move
    */
  def search(gameState: GameState, depth: Int, alpha: Int, beta: Int): Int = {
    if (depth == 0) return return score(gameState)

    val moves = getAllLegalMoves(gameState)

    if (moves.isEmpty) {
      if (isCurrentPlayerInCheck(gameState)) negativeInfinity
      else 0
    } else {
      ParSeq
        .fromSpecific(moves)
        .map({ case (startPos, endPos, pieceType) =>
          val updatedGameState = updateGameState(gameState, startPos, endPos, pieceType)

          -1 * search(updatedGameState, depth - 1, alpha, beta)
        })
        .reduce(max)
    }
  }
}
