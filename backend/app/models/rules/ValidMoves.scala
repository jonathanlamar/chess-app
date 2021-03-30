package models.rules

import models.actions.UpdateGameState.updateGameState
import models.rules.Check.{getAttackSquares, isPlayerInCheck, isCurrentPlayerInCheck}
import models.utils.DataTypes._
import scala.math.{abs, sqrt, max}

/** Logic for generating all valid moves for a piece. */
object ValidMoves {

  // FIXME: Does not properly filter out en passant or limit moves
  def getLegalMoves(gameState: GameState, pos: Position): List[Position] = {
    val kingPos = gameState.piecesIndex.get(Piece(gameState.whoseMove, King)) match {
      case Some(value) =>
        value match {
          case head :: _ => head
          case Nil       => throw new Exception("Empty king position list")
        }
      case None => throw new Exception("No king on board.")
    }
    val opponentColor = if (gameState.whoseMove == White) Black else White
    val pseudoLegalMoves = allPossibleMoves(gameState, pos)

    if (pos == kingPos) {
      pseudoLegalMoves.diff(getAttackSquares(gameState, opponentColor))
    } else if (isCurrentPlayerInCheck(gameState)) {
      pseudoLegalMoves // TODO limit moves.
    } else {
      val oppSlidingPieces = gameState.piecesIndex.view
        .filterKeys({
          case Piece(opponentColor, Bishop) => true
          case Piece(opponentColor, Rook)   => true
          case Piece(opponentColor, Queen)  => true
          case _                            => false
        })
        .toList
        .map({ case (p, poss) => poss.map((p.pieceType, _)) })
        .flatten

      for ((pieceType, oppPos) <- oppSlidingPieces) {
        getDelta(oppPos, kingPos, pieceType) match {
          case Some(delta) => {
            val fullRay = getRayToKing(gameState, oppPos, delta)

            if (posIsBlocking(gameState, pos, fullRay)) {
              return pseudoLegalMoves.intersect(fullRay.map(_._1))
            } else if (
              gameState.squares(pos.row)(pos.col) == Piece(gameState.whoseMove, Pawn) &&
              pseudoLegalMoves.contains(gameState.enPassantTarget) &&
              enPassantBlocking(gameState, pos, fullRay)
            ) {
              return pseudoLegalMoves.filter(_ != gameState.enPassantTarget)
            }
          }
          case None => () // do nothing
        }
      }

      pseudoLegalMoves
    }
  }

  def getEnPassantPiecePos(gameState: GameState): Position = {
    val delta = if (gameState.whoseMove == White) Position(1, 0) else Position(-1, 0)

    gameState.enPassantTarget + delta
  }

  def getDelta(fromPos: Position, toPos: Position, movingPieceType: PieceType): Option[Position] = {
    val diff = toPos - fromPos
    val m = max(abs(diff.row), abs(diff.col)).toDouble
    val maybeDelta =
      if ((diff.row / m).isWhole && (diff.col / m).isWhole)
        Some(Position((diff.row / m).toInt, (diff.col / m).toInt))
      else None

    maybeDelta.flatMap(delta =>
      if (
        (movingPieceType == Rook && isDiagonal(delta)) ||
        (movingPieceType == Bishop && isStraight(delta)) ||
        movingPieceType == Queen
      ) Some(delta)
      else None
    )
  }

  def isDiagonal(delta: Position): Boolean = abs(delta.row) == 1 && abs(delta.col) == 1

  def isStraight(delta: Position): Boolean =
    (abs(delta.row) == 1 && delta.col == 0) || (delta.row == 0 && abs(delta.col) == 1)

  // TODO - refactor to positionsBlockCapture(gs, positions: List[Position], rayToKing): Boolean
  def posIsBlocking(
      gameState: GameState,
      pos: Position,
      rayToKing: List[(Position, Square)]
  ): Boolean = {
    val squares = rayToKing.map(_._2)
    val positions = rayToKing.map(_._1)
    val otherSquares = rayToKing.drop(1).dropRight(1).filter(_._1 != pos).map(_._2)

    positions.contains(pos) &&
    squares.last == Piece(gameState.whoseMove, King) &&
    otherSquares.filter(!_.isBlank).isEmpty
  }

  def enPassantBlocking(
      gameState: GameState,
      pawnPos: Position,
      rayToKing: List[(Position, Square)]
  ): Boolean = {
    val enPassantPiecePos = getEnPassantPiecePos(gameState)
    val squares = rayToKing.map(_._2)
    val positions = rayToKing.map(_._1)
    val otherSquares = rayToKing
      .drop(1)
      .dropRight(1)
      .filter({ case (p, _) => p != enPassantPiecePos && p != pawnPos })
      .map(_._2)

    positions.contains(enPassantPiecePos) &&
    squares.last == Piece(gameState.whoseMove, King) &&
    otherSquares.filter(!_.isBlank).isEmpty
  }

  def getRayToKing(
      gameState: GameState,
      pos: Position,
      delta: Position
  ): List[(Position, Square)] = {
    val rayPieces = List
      .range(0, 8)
      .map(pos + delta * _)
      .filter(_.isInBounds)
      .map(p => (p, gameState.squares(p.row)(p.col)))

    val nonKingSquares = rayPieces.takeWhile(_._2 != Piece(gameState.whoseMove, King))
    val kingSquares = rayPieces
      .dropWhile(_._2 != Piece(gameState.whoseMove, King))
      .takeWhile(_._2 == Piece(gameState.whoseMove, King))

    kingSquares match {
      case Nil       => nonKingSquares
      case head :: _ => nonKingSquares :+ head
    }
  }

  def isCastleMove(gameState: GameState, pos: Position, newPos: Position): Boolean = {
    if (
      gameState.squares(pos.row)(pos.col) == Piece(gameState.whoseMove, King) &&
      pos.row == newPos.row &&
      abs(pos.col - newPos.col) == 2
    ) true
    else false
  }

  def allPossibleMoves(gameState: GameState, pos: Position): List[Position] = {
    gameState.squares(pos.row)(pos.col) match {
      case Piece(color, pieceType) =>
        if (color != gameState.whoseMove) throw new Exception("Wrong color to move")
        else
          pieceType match {
            case Bishop => allPossibleBishopMoves(gameState, pos, color)
            case Rook   => allPossibleRookMoves(gameState, pos, color)
            case Queen  => allPossibleQueenMoves(gameState, pos, color)
            case Pawn   => allPossiblePawnMoves(gameState, pos, color)
            case Knight => allPossibleKnightMoves(gameState, pos, color)
            case King   => allPossibleKingMoves(gameState, pos, color)
          }
      case Blank => throw new Exception("No piece at position")
    }
  }

  def allPossibleKingMoves(gameState: GameState, pos: Position, color: Color): List[Position] = {
    val deltas = List(
      Position(-1, -1),
      Position(-1, 0),
      Position(-1, 1),
      Position(0, -1),
      Position(0, 1),
      Position(1, -1),
      Position(1, 0),
      Position(1, 1)
    )

    val allMoveDeltas =
      if (!isCurrentPlayerInCheck(gameState))
        deltas ::: getCastlePositions(gameState, color)
      else deltas

    doBasicFilters(pos, color, allMoveDeltas)
      .filter(p =>
        gameState.squares(p.row)(p.col).isBlank || gameState.squares(p.row)(p.col).color != color
      )
  }

  def getCastlePositions(gameState: GameState, color: Color): List[Position] = {
    val canCastle = color match {
      case Black =>
        List(
          gameState.castleStatus.blackKing && gameState.squares(0).slice(5, 7).forall(_.isBlank),
          gameState.castleStatus.blackQueen && gameState.squares(0).slice(1, 4).forall(_.isBlank)
        )
      case White =>
        List(
          gameState.castleStatus.whiteKing && gameState.squares(7).slice(5, 7).forall(_.isBlank),
          gameState.castleStatus.whiteQueen && gameState.squares(7).slice(1, 4).forall(_.isBlank)
        )
    }

    List(Position(0, 2), Position(0, -2)).zip(canCastle).filter(_._2).map(_._1)
  }

  def allPossiblePawnMoves(gameState: GameState, pos: Position, color: Color): List[Position] = {
    val deltas =
      if (isInitialPawn(pos, color)) List(Position(-1, 0), Position(-2, 0))
      else List(Position(-1, 0))
    val normalMovePieces = doBasicFilters(pos, color, deltas)
      .map(p => (p, gameState.squares(p.row)(p.col)))
      .takeWhile(_._2.isBlank)
      .map(_._1)

    getPawnCaptureSquares(gameState, pos, color) ::: normalMovePieces
  }

  def isInitialPawn(pos: Position, color: Color): Boolean = {
    color match {
      case Black => pos.row == 1
      case White => pos.row == 6
    }
  }

  def getPawnCaptureSquares(gameState: GameState, pos: Position, color: Color): List[Position] = {
    doBasicFilters(pos, color, List(Position(-1, -1), Position(-1, 1)))
      .filter(p =>
        (!gameState.squares(p.row)(p.col).isBlank &&
          gameState.squares(p.row)(p.col).color != color) || p == gameState.enPassantTarget
      )
  }

  def allPossibleKnightMoves(
      gameState: GameState,
      pos: Position,
      color: Color
  ): List[Position] = {
    val deltas = List(
      Position(-2, -1),
      Position(-2, 1),
      Position(-1, -2),
      Position(-1, 2),
      Position(1, -2),
      Position(1, 2),
      Position(2, -1),
      Position(2, 1)
    )

    doBasicFilters(pos, color, deltas)
      .filter(p =>
        gameState.squares(p.row)(p.col).isBlank || gameState.squares(p.row)(p.col).color != color
      )
  }

  def doBasicFilters(pos: Position, color: Color, deltas: List[Position]): List[Position] = {
    deltas
      .map(delta =>
        color match {
          case Black => pos + delta.verticalFlip
          case White => pos + delta
        }
      )
      .filter(_.isInBounds)
  }

  def allPossibleQueenMoves(gameState: GameState, pos: Position, color: Color): List[Position] = {
    allPossibleBishopMoves(gameState, pos, color) ::: allPossibleRookMoves(gameState, pos, color)
  }

  def allPossibleBishopMoves(gameState: GameState, pos: Position, color: Color): List[Position] = {
    List.concat(
      getRayPositions(gameState, pos, Position(1, 1), color),
      getRayPositions(gameState, pos, Position(-1, 1), color),
      getRayPositions(gameState, pos, Position(1, -1), color),
      getRayPositions(gameState, pos, Position(-1, -1), color)
    )
  }

  def allPossibleRookMoves(gameState: GameState, pos: Position, color: Color): List[Position] = {
    List.concat(
      getRayPositions(gameState, pos, Position(-1, 0), color),
      getRayPositions(gameState, pos, Position(1, 0), color),
      getRayPositions(gameState, pos, Position(0, -1), color),
      getRayPositions(gameState, pos, Position(0, 1), color)
    )
  }

  /** Ray extending from position until out of bounds, or to first opponent piece.
    * @param gameState - game board to consider
    * @param pos - position from which ray is computed
    * @param delta - direction of ray
    * @param color - color of piece in position (not checked)
    * @return list of positions of squares in ray
    */
  def getRayPositions(
      gameState: GameState,
      pos: Position,
      delta: Position,
      color: Color
  ): List[Position] = getRay(gameState, pos, delta, color).map(_._1)

  /** Ray extending from position until out of bounds, or to first opponent piece.
    * @param gameState - game board to consider
    * @param pos - position from which ray is computed
    * @param delta - direction of ray
    * @param color - color of piece in position (not checked)
    * @return list of squares in ray
    */
  def getRaySquares(
      gameState: GameState,
      pos: Position,
      delta: Position,
      color: Color
  ): List[Square] = getRay(gameState, pos, delta, color).map(_._2)

  private def getRay(
      gameState: GameState,
      pos: Position,
      delta: Position,
      color: Color
  ): List[(Position, Square)] = {
    val rayPieces = List
      .range(1, 8)
      .map(pos + delta * _)
      .filter(_.isInBounds)
      .map(p => (p, gameState.squares(p.row)(p.col)))

    val blankRaySquares = rayPieces.takeWhile(_._2.isBlank)
    val otherColorSquares =
      rayPieces.dropWhile(_._2.isBlank).takeWhile(_._2.color != color)

    otherColorSquares match {
      case Nil       => blankRaySquares
      case head :: _ => blankRaySquares :+ head
    }
  }
}
