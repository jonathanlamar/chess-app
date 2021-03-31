package models.ai.minimax

import models.utils.DataTypes._

object Scoring {
  // TODO: Rank based on piece type as well as position and other characteristics
  val pieceScores: Map[PieceType, Int] = Map(
    (Pawn, 100),
    (Knight, 300),
    (Bishop, 300),
    (Rook, 500),
    (Queen, 900),
    (King, 0) // Not sure about this
  )

  /** The score of the game state *for the active player* (i.e., whose turn it is)
    *
    * @param gameState - state of game currently
    * @return - unitless number representing the advantage to the current player (negative = bad)
    */
  def score(gameState: GameState): Int = {
    gameState.piecesIndex.toList
      .flatMap({ case (p, poss) => poss.map((p, _)) })
      .map({ case (p, pos) =>
        if (p.color == gameState.whoseMove) pieceScores(p.pieceType)
        else -1 * pieceScores(p.pieceType)
      })
      .sum
  }
}
