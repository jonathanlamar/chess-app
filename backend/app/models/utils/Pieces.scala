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

  def getOptionalDelta(
      fromPos: Position,
      toPos: Position,
      slidingPieceType: PieceType
  ): Option[Position] = {
    val diff = toPos - fromPos
    val m = max(abs(diff.row), abs(diff.col)).toDouble
    val maybeDelta =
      if ((diff.row / m).isWhole && (diff.col / m).isWhole)
        Some(Position((diff.row / m).toInt, (diff.col / m).toInt))
      else None

    maybeDelta.flatMap(delta =>
      if (
        (slidingPieceType == Rook && isDiagonal(delta)) ||
        (slidingPieceType == Bishop && isStraight(delta)) ||
        slidingPieceType == Queen
      ) Some(delta)
      else None
    )
  }

  def getPawnPositions(gameState: GameState, color: Color): List[Position] =
    getPositionsOfType(gameState, color, Pawn)

  def getSlidingPieces(gameState: GameState, color: Color): List[(PieceType, Position)] = {
    val slidingPieces = gameState.piecesIndex.view
      .filterKeys({
        case Piece(color, Bishop) => true
        case Piece(color, Rook)   => true
        case Piece(color, Queen)  => true
        case _                    => false
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
