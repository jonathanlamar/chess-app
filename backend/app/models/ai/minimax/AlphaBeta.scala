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
  val positiveInfinity = Int.MaxValue

  /** Minmax algorithm with alpha/beta pruning. See
    * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
    *
    * @param gameState - current state of play
    * @param depth - max depth of tree to search, where 0 means score current game
    * @param alpha
    * @param beta
    * @return tuple containing best move and its advantage
    */
  def search(
      gameState: GameState,
      depth: Int,
      alpha: Int = negativeInfinity,
      beta: Int = positiveInfinity
  ): (MoveLike, Int) = {
    if (depth == 0) return (NoMove, score(gameState))

    val moves = getAllLegalMoves(gameState)

    if (moves.isEmpty) {
      if (isCurrentPlayerInCheck(gameState)) (NoMove, negativeInfinity)
      else (NoMove, 0)
    } else {
      ParSeq
        .fromSpecific(moves)
        .map(move =>
          move match {
            case m: Move => {
              val updatedGameState = updateGameState(gameState, m)
              val evaluation = -1 * search(updatedGameState, depth - 1, -beta, -alpha)._2

              (m, evaluation)
            }
            case NoMove => throw new Exception("getAllLegalMoves returned nonsense")
          }
        )
        .reduce((x, y) => if (x._2 > y._2) x else y)
    }
  }
}
