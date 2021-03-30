package models.rules

import models.rules.ValidMoves._
import models.utils.DataTypes._

object Check {
  def getAttackSquares(gameState: GameState, color: Color): List[Position] = {
    val attackingPiecePositions =
      gameState.piecesIndex
        .filter({ case (k: Square, v: List[Position]) => k.color == color })
        .values
        .toList
        .flatten

    attackingPiecePositions
      .flatMap(pos => allPossibleMoves(gameState.updateWhoseMove(color), pos))
      .distinct
  }

  def isCurrentPlayerInCheck(gameState: GameState): Boolean =
    isPlayerInCheck(gameState, gameState.whoseMove)

  def isPlayerInCheck(gameState: GameState, color: Color): Boolean = {
    val kingPosition = gameState.piecesIndex(Piece(color, King)).headOption match {
      case None        => throw new Exception("No King position")
      case Some(value) => value
    }
    val opponentColor = if (color == White) Black else White

    val nwRay = getRaySquares(gameState, kingPosition, Position(-1, -1), color)
    val neRay = getRaySquares(gameState, kingPosition, Position(-1, 1), color)
    val swRay = getRaySquares(gameState, kingPosition, Position(1, -1), color)
    val seRay = getRaySquares(gameState, kingPosition, Position(1, 1), color)

    // Pawn:
    if (color == White && nwRay.length == 1 && nwRay.head == Piece(Black, Pawn)) return true
    if (color == White && neRay.length == 1 && neRay.head == Piece(Black, Pawn)) return true
    if (color == Black && swRay.length == 1 && swRay.head == Piece(White, Pawn)) return true
    if (color == Black && seRay.length == 1 && seRay.head == Piece(White, Pawn)) return true

    // Diagonal sliding pieces:
    val diagonalSlidingPieces = List(Piece(opponentColor, Bishop), Piece(opponentColor, Queen))

    if (!swRay.isEmpty && diagonalSlidingPieces.contains(swRay.last)) return true
    if (!seRay.isEmpty && diagonalSlidingPieces.contains(seRay.last)) return true
    if (!nwRay.isEmpty && diagonalSlidingPieces.contains(nwRay.last)) return true
    if (!neRay.isEmpty && diagonalSlidingPieces.contains(neRay.last)) return true

    // Straight sliding pieces
    val wRay = getRaySquares(gameState, kingPosition, Position(-1, 0), color)
    val nRay = getRaySquares(gameState, kingPosition, Position(0, -1), color)
    val eRay = getRaySquares(gameState, kingPosition, Position(0, 1), color)
    val sRay = getRaySquares(gameState, kingPosition, Position(1, 0), color)
    val straightSlidingPieces = List(Piece(opponentColor, Rook), Piece(opponentColor, Queen))

    if (!wRay.isEmpty && straightSlidingPieces.contains(wRay.last)) return true
    if (!eRay.isEmpty && straightSlidingPieces.contains(eRay.last)) return true
    if (!nRay.isEmpty && straightSlidingPieces.contains(nRay.last)) return true
    if (!sRay.isEmpty && straightSlidingPieces.contains(sRay.last)) return true

    // Knight moves
    val knightMoveSquares = List(
      Position(-2, -1),
      Position(-2, 1),
      Position(-1, -2),
      Position(-1, 2),
      Position(1, -2),
      Position(1, 2),
      Position(2, -1),
      Position(2, 1)
    )
      .map(_ + kingPosition)
      .filter(_.isInBounds)
      .map(pos => gameState.squares(pos.row)(pos.col))

    if (knightMoveSquares.contains(Piece(opponentColor, Knight))) return true

    // King attacks.
    if (nwRay.length == 1 && nwRay.head == Piece(opponentColor, King)) return true
    if (neRay.length == 1 && neRay.head == Piece(opponentColor, King)) return true
    if (swRay.length == 1 && swRay.head == Piece(opponentColor, King)) return true
    if (seRay.length == 1 && seRay.head == Piece(opponentColor, King)) return true
    if (wRay.length == 1 && wRay.head == Piece(opponentColor, King)) return true
    if (nRay.length == 1 && nRay.head == Piece(opponentColor, King)) return true
    if (eRay.length == 1 && eRay.head == Piece(opponentColor, King)) return true
    if (sRay.length == 1 && sRay.head == Piece(opponentColor, King)) return true

    return false
  }

  def getCurrentPlayerCheckStatus(gameState: GameState): CheckStatus = {
    getCheckStatus(gameState, gameState.whoseMove)
  }

  def getCheckStatus(gameState: GameState, color: Color): CheckStatus = {
    lazy val allLegalMoves = gameState.piecesIndex
      .filter({ case (k: Square, v: List[Position]) => k.color == color })
      .values
      .toList
      .flatten
      .flatMap(pos => getLegalMoves(gameState.updateWhoseMove(color), pos))
      .distinct

    CheckStatus(isPlayerInCheck(gameState, color), allLegalMoves.isEmpty)
  }

  /** Helper functions */
  def getPiecesAttackingKing(
      gameState: GameState,
      kingColor: Color
  ): List[(PieceType, Position)] = {
    val oppoColor = if (kingColor == White) Black else White
    val kingPosition = getKingPosition(gameState, kingColor) match {
      case None        => throw new Exception("No king for check")
      case Some(value) => value
    }

    val attackingPawns = getPawnPositions(gameState, oppoColor)
      .filter(p => allPossiblePawnMoves(gameState, p, oppoColor).contains(kingPosition))
      .map((Pawn, _))

    val attackingKnights = getKnightPositions(gameState, oppoColor)
      .filter(p => allPossibleKnightMoves(gameState, p, oppoColor).contains(kingPosition))
      .map((Knight, _))

    val attackingSlidingPieces = getSlidingPieces(gameState, oppoColor)
      .filter({
        case (t, p) => {
          getDelta(p, kingPosition, t) match {
            case None        => false
            case Some(delta) => getRayPositions(gameState, p, delta, oppoColor).last == kingPosition
          }
        }
      })

    val attackingKing = getKingPosition(gameState, oppoColor)
      .flatMap(p =>
        if (allPossibleKingMoves(gameState, p, oppoColor).contains(kingPosition)) Some((King, p))
        else None
      )
      .toList

    attackingPawns ::: attackingKnights ::: attackingSlidingPieces ::: attackingKing
  }

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

  def getQueenPosition(gameState: GameState, color: Color): Option[Position] =
    getPositionsOfType(gameState, color, Queen).headOption

  def getKingPosition(gameState: GameState, color: Color): Option[Position] =
    getPositionsOfType(gameState, color, King).headOption
}
