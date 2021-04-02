package models.utils

import models.rules.ValidMoves._
import models.utils.DataTypes._
import scala.math.{abs, max}

/** Util functions for looking up pieces, as well as getting attacks. */
object Pieces {

  def getEnPassantPiecePos(gameState: GameState): Position = {
    val delta = if (gameState.whoseMove == White) Position(1, 0) else Position(-1, 0)

    gameState.enPassantTarget + delta
  }

  def getOptionalDelta(move: Move, slidingPieceType: PieceType): Option[Position] = {
    val diff = move.to - move.from
    val m = max(abs(diff.row), abs(diff.col)).toDouble
    val maybeDelta =
      if ((diff.row / m).isWhole && (diff.col / m).isWhole)
        Some(Position((diff.row / m).toInt, (diff.col / m).toInt))
      else None

    maybeDelta.flatMap(delta =>
      if (
        (slidingPieceType == Rook && isStraight(delta)) ||
        (slidingPieceType == Bishop && isDiagonal(delta)) ||
        slidingPieceType == Queen
      ) Some(delta)
      else None
    )
  }

  def getAttackingPieces(gameState: GameState, attackColor: Color): List[(PieceType, Position)] = {
    val kingColor = attackColor.reverse
    val kingPos = getKingPosition(gameState, kingColor) match {
      case None        => throw new Exception("No king to attack")
      case Some(value) => value
    }

    val pawns = getPawnPositions(gameState, attackColor)
      .map(p => (p, allPossiblePawnMoves(gameState, p, attackColor)))
      .filter({ case (p, poss) => poss.contains(kingPos) })
      .map { case (p, _) => (Pawn, p) }

    val knights = getKnightPositions(gameState, attackColor)
      .map(p => (p, allPossibleKnightMoves(gameState, p, attackColor)))
      .filter({ case (p, poss) => poss.contains(kingPos) })
      .map { case (p, _) => (Knight, p) }

    val kings = getKingPosition(gameState, attackColor).toList
      .map({ p =>
        (p, allPossibleKingMoves(gameState, p, attackColor))
      })
      .filter({ case (p, poss) => poss.contains(kingPos) })
      .map({ case (p, _) => (King, p) })

    val slidingPieces = getSlidingPieces(gameState, attackColor)
      .flatMap({ case (pieceType, oppPos) =>
        val move = Move(oppPos, kingPos)
        getOptionalDelta(move, pieceType).map((pieceType, oppPos, _))
      })
      .filter({ case (_, oppPos, delta) =>
        getRayPositions(gameState, oppPos, delta, attackColor).contains(kingPos)
      })
      .map({ case (pieceType, oppPos, _) => (pieceType, oppPos) })

    pawns ::: knights ::: kings ::: slidingPieces
  }

  def getPawnPositions(gameState: GameState, color: Color): List[Position] =
    getPositionsOfType(gameState, color, Pawn)

  def getSlidingPieces(gameState: GameState, pieceColor: Color): List[(PieceType, Position)] = {
    val slidingPieces = gameState.piecesIndex.view
      .filterKeys({
        case Piece(c, t) => c == pieceColor && List(Bishop, Rook, Queen).contains(t)
        case _           => false
      })
      .toList
      .flatMap({ case (p, poss) => poss.map((p.pieceType, _)) })

    slidingPieces
  }

  def getKnightPositions(gameState: GameState, color: Color): List[Position] =
    getPositionsOfType(gameState, color, Knight)

  def getKingPosition(gameState: GameState, color: Color): Option[Position] =
    getPositionsOfType(gameState, color, King).headOption

  private def isDiagonal(delta: Position): Boolean = abs(delta.row) == 1 && abs(delta.col) == 1

  private def isStraight(delta: Position): Boolean =
    (abs(delta.row) == 1 && delta.col == 0) || (delta.row == 0 && abs(delta.col) == 1)

  private def getPositionsOfType(
      gameState: GameState,
      color: Color,
      pieceType: PieceType
  ): List[Position] = {
    gameState.piecesIndex.view
      .filterKeys(_ == Piece(color, pieceType))
      .values
      .toList
      .flatten
  }
}
