package models.ai.minimax

import models.actions.UpdateGameState.updateGameState
import models.ai.minimax.Scoring._
import models.ai.minimax.Zobrist
import models.rules.Check.isCurrentPlayerInCheck
import models.rules.ValidMoves._
import models.utils.DataTypes._
import scala.collection.mutable.HashMap
import scala.math.{min, max, abs}

class AlphaBeta {
  val negativeInfinity = Int.MinValue
  val positiveInfinity = Int.MaxValue
  var zobristHash: HashMap[Long, Int] = new HashMap()
  val zobristHashOb = new Zobrist()

  def makeMove(gameState: GameState): GameState = {
    val hashVal = zobristHashOb.computeHash(gameState)
    val move = search(gameState, 20, hashVal = hashVal)._1

    updateGameState(gameState, move)
  }

  /** Minmax algorithm with alpha/beta pruning. See
    * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
    *
    * Alpha beta pruning differs from standard minmax by pruning branches that
    * cannot affect the outcome.  It does this by keeping track of the maximum
    * and minimum scores asured to the minimizing and maximizing players,
    * respectively.
    *
    * This algorithm is inherently serial, since the search of nodes depends on
    * knowledge of previous searched nodes.
    *
    * @param gameState - current state of play
    * @param depth - max depth of tree to search, where 0 means score current game
    * @param alpha - the minimum score that the maximizing player is assured of
    * @param beta - the maximum score that the minimizing player is assured of
    * @param maximizingPlayer - whether we are maximizing advantage or minimizing disadvantage
    * @param hashVal - Value of Zobrist hash of initial game state
    * @return tuple containing best move and its advantage
    */
  def search(
      gameState: GameState,
      depth: Int,
      alpha: Int = negativeInfinity,
      beta: Int = positiveInfinity,
      maximizingPlayer: Boolean = true,
      hashVal: Long
  ): (MoveLike, Int) = {
    if (depth == 0) {
      val thisScore =
        if (zobristHash.contains(hashVal)) zobristHash(hashVal)
        else {
          val score = searchAllCaptures(gameState, alpha, beta)

          zobristHash.put(hashVal, score)
          score
        }

      if (maximizingPlayer) return (NoMove, thisScore)
      else return (NoMove, -thisScore)
    }

    implicit val moveOrdering: Ordering[MoveLike] = Ordering.by(moveScore(gameState)).reverse
    val moves = getAllLegalMoves(gameState).sorted

    if (moves.isEmpty) {
      if (isCurrentPlayerInCheck(gameState)) (NoMove, negativeInfinity)
      else (NoMove, 0)
    } else if (maximizingPlayer) {
      var bestScore = negativeInfinity
      var bestMove: MoveLike = NoMove
      var newAlpha = alpha

      for (move <- moves) {

        val updatedGameState = updateGameState(gameState, move)
        val updatedHash = zobristHashOb.updateHash(gameState, move, hashVal)
        val newScore =
          if (zobristHash.contains(updatedHash)) zobristHash(updatedHash)
          else {
            val score = search(updatedGameState, depth - 1, newAlpha, beta, false, updatedHash)._2

            zobristHash.put(updatedHash, score)
            score
          }

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
        val updatedHash = zobristHashOb.updateHash(gameState, move, hashVal)
        val newScore =
          if (zobristHash.contains(updatedHash)) zobristHash(updatedHash)
          else {
            val score = search(updatedGameState, depth - 1, alpha, newBeta, true, updatedHash)._2

            zobristHash.put(updatedHash, score)
            score
          }

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

  /** Evaluate game state based on availability of capture moves.  Taken
    * wholesale from the chess AI video.  All credit to Sebastian Lague.
    *
    * Captures aren't typically forces, so see what the eval is before making a
    * capture.  Otherwise if only bad captures are available, the position will
    * be evaluated as bad, even if good non-capture moves exist.
    *
    * @param gameState
    * @param alpha - state of alpha mid-pruning algorithm
    * @param beta - state of beta mid-pruning algorithm (at leaf node)
    * @return eval based on capture availability
    */
  def searchAllCaptures(gameState: GameState, alpha: Int, beta: Int): Int = {
    var eval = score(gameState)
    if (eval >= beta) return beta

    var newAlpha = max(alpha, eval)

    implicit val moveOrdering: Ordering[MoveLike] = Ordering.by(moveScore(gameState)).reverse
    val captureMoves = getAllLegalMoves(gameState)
      .filter({
        case NoMove                          => false
        case Move(from, to, promotePawnType) => !gameState.squares(to).isBlank
      })
      .sorted

    for (move <- captureMoves) {
      val updatedGameState = updateGameState(gameState, move)
      eval = -searchAllCaptures(updatedGameState, -beta, -newAlpha)

      if (eval >= beta) return beta

      newAlpha = max(newAlpha, eval)
    }

    newAlpha
  }
}
