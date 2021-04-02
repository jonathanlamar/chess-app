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

  def moveScore(gameState: GameState)(moveLike: MoveLike): Int = moveLike match {
    case NoMove => 0
    case Move(from, to, _) => {
      var moveScoreGuess = 0
      val movePieceType = gameState.squares(from) match {
        case Blank               => throw new Exception("Eyy, tryan'a move a blank square")
        case Piece(_, pieceType) => pieceType
      }

      gameState.squares(to) match {
        case Blank => ()
        case Piece(_, pieceType) =>
          moveScoreGuess += 10 * pieceScores(pieceType) - pieceScores(movePieceType)
      }

      if (
        movePieceType == Pawn && (
          (gameState.whoseMove == White && to.row == 0) ||
            (gameState.whoseMove == Black && to.row == 7)
        )
      ) moveScoreGuess += pieceScores(movePieceType)

      if (gameState.opponentAttackSquares(to))
        moveScoreGuess -= pieceScores(movePieceType)

      moveScoreGuess
    }
  }
}
