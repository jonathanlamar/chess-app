package models.rules

import models.utils.DataTypes._

object ValidMoves {

  // TODO: Castling
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
    val moves =
      pawnCaptureSquares(board, pos, color) ::: allPossibleJumpMoves(board, pos, deltas, color)

    moves
  }

  def isInitialPawn(pos: Position, color: Color): Boolean = {
    color match {
      case Black => pos.row == 1
      case White => pos.row == 6
    }
  }

  def pawnCaptureSquares(board: Board, pos: Position, color: Color): List[Position] = {
    List(Position(-1, -1), Position(-1, 1))
      .map(delta =>
        color match {
          case Black => delta.verticalFlip
          case White => delta
        }
      )
      .map(pos + _)
      .filter(_.isInBounds)
      .filter(p => board.squares(p.row)(p.col) != Blank || p == board.enPassantTarget)
  }

  val jumpDeltas = Map(
    Pawn -> List(Position(-1, 0)),
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
          case Black => delta.verticalFlip
          case White => delta
        }
      )
      .map(pos + _)
      .filter(_.isInBounds)
      .filter(p => {
        val dest = board.squares(p.row)(p.col)

        dest match {
          case Blank                => true
          case Piece(otherColor, _) => otherColor != color
        }
      })
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
    var i = 0
    var keepGoing = true
    var ray: List[Position] = Nil
    while (keepGoing) {
      val toAddPos = pos + delta * i

      if (toAddPos.isInBounds) {
        val pieceAtPos = board.squares(toAddPos.row)(toAddPos.col)

        pieceAtPos match {
          case Blank => ray = toAddPos :: ray
          case Piece(otherColor, _) => {
            keepGoing = false
            if (otherColor != color) ray = toAddPos :: ray
          }
        }
      }
    }

    ray
  }
}
