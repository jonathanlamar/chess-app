package models.rules

import models.utils.DataTypes._

object ValidMoves {

  // TODO: Castling, Pawn promotion
  def allPossibleMoves(board: Board, pos: Position): List[Position] = {
    board.squares(pos.row)(pos.col) match {
      case Piece(color, pieceType) =>
        if (color != board.whoseMove) throw new Exception("Wrong color to move")
        else
          pieceType match {
            case Bishop           => allPossibleBishopMoves(board, pos, color)
            case Rook             => allPossibleRookMoves(board, pos, color)
            case Queen            => allPossibleQueenMoves(board, pos, color)
            case Pawn             => allPossiblePawnMoves(board, pos, color)
            case p: JumpPieceType => allPossibleJumpMoves(board, pos, jumpDeltas(p), color)
          }
      case Blank => throw new Exception("No piece at position")
    }
  }

  def allPossiblePawnMoves(board: Board, pos: Position, color: Color): List[Position] = {
    val deltas =
      if (isInitialPawn(pos, color)) List(Position(-1, 0), Position(-2, 0))
      else List(Position(-1, 0))
    val normalMoves = deltas
      .map(delta =>
        color match {
          case Black => pos + delta.verticalFlip
          case White => pos + delta
        }
      )
      .filter(_.isInBounds)
      .filter(p => board.squares(p.row)(p.col).isBlank)

    getPawnCaptureSquares(board, pos, color) ::: normalMoves
  }

  def isInitialPawn(pos: Position, color: Color): Boolean = {
    color match {
      case Black => pos.row == 1
      case White => pos.row == 6
    }
  }

  def getPawnCaptureSquares(board: Board, pos: Position, color: Color): List[Position] = {
    List(Position(-1, -1), Position(-1, 1))
      .map(delta =>
        color match {
          case Black => delta.verticalFlip
          case White => delta
        }
      )
      .map(pos + _)
      .filter(_.isInBounds)
      .filter(p => !board.squares(p.row)(p.col).isBlank || p == board.enPassantTarget)
  }

  val jumpDeltas = Map(
    Knight -> List(
      Position(-2, -1),
      Position(-2, 1),
      Position(-1, -2),
      Position(-1, 2),
      Position(1, -2),
      Position(1, 2),
      Position(2, -1),
      Position(2, 1)
    ),
    King -> List(
      Position(-1, -1),
      Position(-1, 0),
      Position(-1, 1),
      Position(0, -1),
      Position(0, 1),
      Position(1, -1),
      Position(1, 0),
      Position(1, 1)
    )
  )

  def allPossibleJumpMoves(
      board: Board,
      pos: Position,
      deltas: List[Position],
      color: Color
  ): List[Position] = {
    deltas
      .map(delta =>
        color match {
          case Black => pos + delta.verticalFlip
          case White => pos + delta
        }
      )
      .filter(_.isInBounds)
      .filter(p =>
        board.squares(p.row)(p.col).isBlank || board.squares(p.row)(p.col).color != color
      )
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
