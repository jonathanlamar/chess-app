package models.rules

import models.utils.DataTypes._
import scala.math.{abs, signum}

/** Logic for updating the board, given a valid move.
  * In the interest of speed, assumes the move is one of the valid moves
  * proposed by ValidMoves.
  */
object UpdateGameState {
  def updateGameState(
      board: Board,
      movingPiecePos: Position,
      destinationPos: Position
  ): Board = {
    if (movingPiecePos == destinationPos) return board

    val movingPiece = board.squares(movingPiecePos.row)(movingPiecePos.col)
    val destinationSquare = board.squares(destinationPos.row)(destinationPos.col)

    movingPiece match {
      case Blank => throw new Exception("Cannot move blank square")
      case p: Piece => {
        board
          .transform(updateMoveCounts(p, destinationSquare))
          .transform(handleCapture(p, destinationSquare, destinationPos))
          .transform(handleCastling(movingPiecePos, p, destinationPos))
          .transform(updateEnPassantTarget(movingPiecePos, p, destinationPos))
          .transform(updateCastleStatus(p, movingPiecePos))
          .transform(updatePiecePosition(movingPiecePos, p, destinationPos))
      }
    }
  }

  def updateMoveCounts(piece: Piece, destination: Square)(board: Board): Board = {
    val halfMoveClock =
      if (piece.pieceType == Pawn || destination.isBlank) 0 else board.halfMoveClock + 1
    val fullMoveCount = if (piece.color == Black) board.fullMoveCount + 1 else board.fullMoveCount

    board.updateHalfMoveClock(halfMoveClock).updateFullMoveCount(fullMoveCount)
  }

  def handleCapture(piece: Piece, destination: Square, destinationPos: Position)(
      board: Board
  ): Board = {
    if (piece.pieceType == Pawn && destinationPos == board.enPassantTarget) {
      // En passant capturing is the weird edge case
      piece.color match {
        case Black => {
          board.squares(board.enPassantTarget.row - 1)(board.enPassantTarget.col) match {
            case p: Piece => {
              board.addBlackCapturedPiece(p)
              board.updateSquare(board.enPassantTarget + Position(-1, 0), Blank)
            }
            case Blank => throw new Exception("En Passant capture is blank")
          }
        }
        case White => {
          board.squares(board.enPassantTarget.row + 1)(board.enPassantTarget.col) match {
            case p: Piece => {
              board.addWhiteCapturedPiece(p)
              board.updateSquare(board.enPassantTarget + Position(1, 0), Blank)
            }
            case Blank => throw new Exception("En Passant capture is blank")
          }
        }
      }
    } else {
      destination match {
        case p: Piece =>
          piece.color match {
            case Black => {
              board.addBlackCapturedPiece(p)
              board.updateSquare(destinationPos, Blank)
            }
            case White => {
              board.addWhiteCapturedPiece(p)
              board.updateSquare(destinationPos, Blank)
            }
          }
        case Blank => board
      }
    }
  }

  def handleCastling(movingPiecePos: Position, movingPiece: Piece, destinationPos: Position)(
      board: Board
  ): Board = {
    if (movingPiece.pieceType != King) board
    else {
      val deltaC = destinationPos.col - movingPiecePos.col

      if (movingPiecePos.row == destinationPos.row && abs(deltaC) == 2) {
        val kingRow = if (movingPiece.color == White) 7 else 0
        val rookCol = if (signum(deltaC) == 1) 7 else 0

        board
          .updateSquare(Position(kingRow, rookCol), Blank)
          .updateSquare(Position(kingRow, 4 + signum(deltaC)), Piece(movingPiece.color, Rook))
      } else board
    }
  }

  def updateEnPassantTarget(movingPiecePos: Position, movingPiece: Piece, destinationPos: Position)(
      board: Board
  ): Board = {
    if (movingPiece.pieceType == Pawn && abs(destinationPos.row - movingPiecePos.row) == 2) {
      board.updateEnPassantTarget(
        Position((movingPiecePos.row + destinationPos.row) / 2, movingPiecePos.col)
      )
    } else board
  }

  def updateCastleStatus(movingPiece: Piece, movingPiecePos: Position)(board: Board): Board = {
    movingPiece.pieceType match {
      case Rook =>
        movingPiece.color match {
          case White =>
            if (board.castleStatus.whiteQueen && movingPiecePos == Position(7, 0)) {
              board.updateCastleStatus(Piece(White, Queen), false)
            } else if (board.castleStatus.whiteKing && movingPiecePos == Position(7, 7)) {
              board.updateCastleStatus(Piece(White, King), false)
            } else board
          case Black =>
            if (board.castleStatus.blackQueen && movingPiecePos == Position(0, 0)) {
              board.updateCastleStatus(Piece(Black, Queen), false)
            } else if (board.castleStatus.blackKing && movingPiecePos == Position(0, 7)) {
              board.updateCastleStatus(Piece(Black, King), false)
            } else board
        }
      case _ => board
    }
  }

  def updatePiecePosition(movingPiecePos: Position, movingPiece: Piece, destinationPos: Position)(
      board: Board
  ): Board = {
    board.updateSquare(movingPiecePos, Blank).updateSquare(destinationPos, movingPiece)
  }
}
