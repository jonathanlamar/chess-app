package models.rules

import models.utils.DataTypes._

object ValidMoves {

  // TODO: Pawn promotion
  def allPossibleMoves(board: Board, pos: Position): List[Position] = {
    board.squares(pos.row)(pos.col) match {
      case Piece(color, pieceType) =>
        if (color != board.whoseMove) throw new Exception("Wrong color to move")
        else
          pieceType match {
            case Bishop => allPossibleBishopMoves(board, pos, color)
            case Rook   => allPossibleRookMoves(board, pos, color)
            case Queen  => allPossibleQueenMoves(board, pos, color)
            case Pawn   => allPossiblePawnMoves(board, pos, color)
            case Knight => allPossibleKnightMoves(board, pos, color)
            case King   => allPossibleKingMoves(board, pos, color)
          }
      case Blank => throw new Exception("No piece at position")
    }
  }

  def allPossibleKingMoves(board: Board, pos: Position, color: Color): List[Position] = {
    val deltas = List(
      Position(-1, -1),
      Position(-1, 0),
      Position(-1, 1),
      Position(0, -1),
      Position(0, 1),
      Position(1, -1),
      Position(1, 0),
      Position(1, 1)
    ) ::: getCastlePositions(board, color)

    doBasicFilters(pos, color, deltas)
      .filter(p =>
        board.squares(p.row)(p.col).isBlank || board.squares(p.row)(p.col).color != color
      )
  }

  def getCastlePositions(board: Board, color: Color): List[Position] = {
    val canCastle = color match {
      case Black =>
        List(
          board.castleStatus.blackKing && board.squares(0).slice(5, 7).forall(_.isBlank),
          board.castleStatus.blackQueen && board.squares(0).slice(1, 4).forall(_.isBlank)
        )
      case White =>
        List(
          board.castleStatus.whiteKing && board.squares(7).slice(5, 7).forall(_.isBlank),
          board.castleStatus.whiteQueen && board.squares(7).slice(1, 4).forall(_.isBlank)
        )
    }

    List(Position(0, 2), Position(0, -2)).zip(canCastle).filter(_._2).map(_._1)
  }

  def allPossiblePawnMoves(board: Board, pos: Position, color: Color): List[Position] = {
    val deltas =
      if (isInitialPawn(pos, color)) List(Position(-1, 0), Position(-2, 0))
      else List(Position(-1, 0))
    val normalMovePieces = doBasicFilters(pos, color, deltas)
        .map(p => (p, board.squares(p.row)(p.col)))
        .takeWhile(_._2.isBlank)
        .map(_._1)

    getPawnCaptureSquares(board, pos, color) ::: normalMovePieces
  }

  def isInitialPawn(pos: Position, color: Color): Boolean = {
    color match {
      case Black => pos.row == 1
      case White => pos.row == 6
    }
  }

  // TODO: This may be a bug
  def getPawnCaptureSquares(board: Board, pos: Position, color: Color): List[Position] = {
    doBasicFilters(pos, color, List(Position(-1, -1), Position(-1, 1)))
      .filter(p =>
        (!board.squares(p.row)(p.col).isBlank &&
          board.squares(p.row)(p.col).color != color) || p == board.enPassantTarget
      )
  }

  def allPossibleKnightMoves(
      board: Board,
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
        board.squares(p.row)(p.col).isBlank || board.squares(p.row)(p.col).color != color
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

  def allPossibleQueenMoves(board: Board, pos: Position, color: Color): List[Position] = {
    allPossibleBishopMoves(board, pos, color) ::: allPossibleRookMoves(board, pos, color)
  }

  def allPossibleBishopMoves(board: Board, pos: Position, color: Color): List[Position] = {
    List.concat(
      getRay(board, pos, Position(1, 1), color),
      getRay(board, pos, Position(-1, 1), color),
      getRay(board, pos, Position(1, -1), color),
      getRay(board, pos, Position(-1, -1), color)
    )
  }

  def allPossibleRookMoves(board: Board, pos: Position, color: Color): List[Position] = {
    List.concat(
      getRay(board, pos, Position(-1, 0), color),
      getRay(board, pos, Position(1, 0), color),
      getRay(board, pos, Position(0, -1), color),
      getRay(board, pos, Position(0, 1), color)
    )
  }

  def getRay(board: Board, pos: Position, delta: Position, color: Color): List[Position] = {
    val rayPieces = (1 until 8).toList
      .map(pos + delta * _)
      .filter(_.isInBounds)
      .map(p => (p, board.squares(p.row)(p.col)))

    val blankRaySquares = rayPieces.takeWhile(_._2.isBlank).map(_._1)
    val otherColorSquares =
      rayPieces.dropWhile(_._2.isBlank).takeWhile(_._2.color != color).map(_._1)

    otherColorSquares match {
      case Nil       => blankRaySquares
      case head :: _ => blankRaySquares :+ head
    }
  }
}
