package models.ai.random

import models.actions.UpdateGameState.updateGameState
import models.rules.ValidMoves.getAllLegalMoves
import models.utils.DataTypes._
import scala.util.Random

object RandomAi {
  def makeMove(gameState: GameState): GameState = {
    val allMoves = getAllLegalMoves(gameState)

    if (allMoves.isEmpty) gameState
    else {
      val move = allMoves(Random.nextInt(allMoves.length))

      updateGameState(gameState, move)
    }
  }
}
