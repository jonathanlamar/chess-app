package models.ai

import models.utils.DataTypes.GameState

trait Ai {
  def makeMove(gameState: GameState): GameState
}
