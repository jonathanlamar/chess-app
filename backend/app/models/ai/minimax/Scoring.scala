package models.ai.minimax

import models.utils.DataTypes._
import scala.math.{abs, max}

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
    var numEnemyPieces = 0

    val initialScore = gameState.piecesIndex.toList
      .flatMap({ case (p, poss) => poss.map((p, _)) })
      .map({ case (p, pos) =>
        if (p.color == gameState.whoseMove) pieceScores(p.pieceType)
        else {
          numEnemyPieces += 1
          -1 * pieceScores(p.pieceType)
        }
      })
      .sum

    // FIXME: This greatly degrades performance.
    // val numEnemyPieces = gameState.piecesIndex.view
    //   .filterKeys(_.color != gameState.whoseMove)
    //   .mapValues(_.length)
    //   .values
    //   .sum

    // Maybe don't calculate endgame weight until we are close to the endgame.
    if (numEnemyPieces < 5) {
      val endgameWeight = 1.0 - (numEnemyPieces.toDouble / 16.0)
      val friendlyKingPos = gameState.piecesIndex(Piece(gameState.whoseMove, King)).head
      val enemyKingPos = gameState.piecesIndex(Piece(gameState.whoseMove.reverse, King)).head

      initialScore + forceKingToCornerEndgameEval(friendlyKingPos, enemyKingPos, endgameWeight)
    } else initialScore
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

  /** Another function ripped off from the youtube video.  This to favor
    * endgame positions that put the enemy king near the corners of the board.
    */
  def forceKingToCornerEndgameEval(
      friendlyKingPos: Position,
      enemyKingPos: Position,
      endgameWeight: Double
  ): Int = {
    // Favor positions where opponent king has been forced away from the center
    // (to the edge or corner of the board) This makes it easier to deliver
    // checkmate (in endgame positions)
    val eval = 14 - l1Distance(friendlyKingPos, enemyKingPos) + distToCenter(enemyKingPos)

    (eval * 10 * endgameWeight).toInt
  }

  private def distToCenter(pos: Position): Double = {
    val rowDist = max(3 - pos.row, pos.row - 4)
    val colDist = max(3 - pos.row, pos.row - 4)

    rowDist + colDist
  }

  private def l1Distance(pos1: Position, pos2: Position): Int =
    abs(pos1.row - pos2.row) + abs(pos1.col - pos2.col)
}
