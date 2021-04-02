package models.ai.minimax

import models.actions.UpdateGameState.updateGameState
import models.ai.minimax.Scoring.score
import models.rules.Check.isCurrentPlayerInCheck
import models.rules.ValidMoves._
import models.utils.DataTypes._
import scala.collection.parallel.ParSeq
import scala.math.{min, max}

object AlphaBeta {
  val negativeInfinity = Int.MinValue
  val positiveInfinity = Int.MaxValue

  /** Minmax algorithm with alpha/beta pruning. See
    * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
    * This algorithm is inherently serial, since the search of nodes depends on
    * knowledge of previous searched nodes.
    *
    * @param gameState - current state of play
    * @param depth - max depth of tree to search, where 0 means score current game
    * @param alpha
    * @param beta
    * @param maximizingPlayer
    * @return tuple containing best move and its advantage
    */
  def search(
      gameState: GameState,
      depth: Int,
      alpha: Int = negativeInfinity,
      beta: Int = positiveInfinity,
      maximizingPlayer: Boolean = true
  ): (MoveLike, Int) = {
    if (depth == 0) {
      val thisScore = score(gameState)

      if (maximizingPlayer) return (NoMove, thisScore)
      else return (NoMove, -thisScore)
    }

    val moves = getAllLegalMoves(gameState)

    if (moves.isEmpty) {
      if (isCurrentPlayerInCheck(gameState)) (NoMove, negativeInfinity)
      else (NoMove, 0)
    } else if (maximizingPlayer) {
      var bestScore = negativeInfinity
      var bestMove: MoveLike = NoMove
      var newAlpha = alpha

      for (move <- moves) {

        val updatedGameState = updateGameState(gameState, move)
        val (_, newScore) = search(updatedGameState, depth - 1, newAlpha, beta, false)

        println(s"newScore = ${newScore}")

        if (newScore > bestScore) {
          bestScore = newScore
          bestMove = move
        }

        newAlpha = max(newAlpha, bestScore)

        if (newAlpha >= beta) return (bestMove, bestScore)
      }

      (bestMove, bestScore)
    } else {
      var bestScore = positiveInfinity
      var bestMove: MoveLike = NoMove
      var newBeta = beta

      for (move <- moves) {

        val updatedGameState = updateGameState(gameState, move)
        val (_, newScore) = search(updatedGameState, depth - 1, alpha, newBeta, true)

        println(s"newScore = ${newScore}")

        if (newScore < bestScore) {
          bestScore = newScore
          bestMove = move
        }

        newBeta = min(newBeta, bestScore)

        if (newBeta <= alpha) return (bestMove, -bestScore)
      }

      (bestMove, -bestScore)
    }
  }

  def makeMove(gameState: GameState): GameState = {
    val move = search(gameState, 4)._1

    updateGameState(gameState, move)
  }
}
