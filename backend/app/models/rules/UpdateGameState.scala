package models.rules

import models.utils.DataTypes._
import scala.math.{abs, signum}

/** Logic for updating the game state, given a valid move.
  * In the interest of speed, assumes the move is one of the valid moves
  * proposed by ValidMoves.
  */
object UpdateGameState {
  def updateGameState(
      gameState: GameState,
      movingPiecePos: Position,
      destinationPos: Position
  ): GameState = {
    if (movingPiecePos == destinationPos) return gameState

    val movingPiece = gameState.squares(movingPiecePos.row)(movingPiecePos.col)
    val destinationSquare = gameState.squares(destinationPos.row)(destinationPos.col)

    movingPiece match {
      case Blank => throw new Exception("Cannot move blank square")
      case p: Piece => {
        gameState
          .transform(updateMoveCounts(p, destinationSquare))
          .transform(handleCapture(p, destinationSquare, destinationPos))
          .transform(handleCastling(movingPiecePos, p, destinationPos))
          .transform(updateEnPassantTarget(movingPiecePos, p, destinationPos))
          .transform(updateCastleStatus(p, movingPiecePos))
          .transform(updatePiecePosition(movingPiecePos, p, destinationPos))
      }
    }
  }

  def updateMoveCounts(piece: Piece, destination: Square)(gameState: GameState): GameState = {
    val halfMoveClock =
      if (piece.pieceType == Pawn || destination.isBlank) 0 else gameState.halfMoveClock + 1
    val fullMoveCount =
      if (piece.color == Black) gameState.fullMoveCount + 1 else gameState.fullMoveCount

    gameState.updateHalfMoveClock(halfMoveClock).updateFullMoveCount(fullMoveCount)
  }

  def handleCapture(piece: Piece, destination: Square, destinationPos: Position)(
      gameState: GameState
  ): GameState = {
    if (piece.pieceType == Pawn && destinationPos == gameState.enPassantTarget) {
      // En passant capturing is the weird edge case
      piece.color match {
        case Black => {
          gameState.squares(gameState.enPassantTarget.row - 1)(
            gameState.enPassantTarget.col
          ) match {
            case p: Piece => {
              gameState
                .addBlackCapturedPiece(p)
                .updateSquare(gameState.enPassantTarget + Position(-1, 0), Blank)
            }
            case Blank => throw new Exception("En Passant capture is blank")
          }
        }
        case White => {
          gameState.squares(gameState.enPassantTarget.row + 1)(
            gameState.enPassantTarget.col
          ) match {
            case p: Piece => {
              gameState
                .addWhiteCapturedPiece(p)
                .updateSquare(gameState.enPassantTarget + Position(1, 0), Blank)
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
              gameState.addBlackCapturedPiece(p).updateSquare(destinationPos, Blank)
            }
            case White => {
              gameState.addWhiteCapturedPiece(p).updateSquare(destinationPos, Blank)
            }
          }
        case Blank => gameState
      }
    }
  }

  def handleCastling(movingPiecePos: Position, movingPiece: Piece, destinationPos: Position)(
      gameState: GameState
  ): GameState = {
    if (movingPiece.pieceType != King) gameState
    else {
      val deltaC = destinationPos.col - movingPiecePos.col

      if (movingPiecePos.row == destinationPos.row && abs(deltaC) == 2) {
        val kingRow = if (movingPiece.color == White) 7 else 0
        val rookCol = if (signum(deltaC) == 1) 7 else 0

        gameState
          .updateSquare(Position(kingRow, rookCol), Blank)
          .updateSquare(Position(kingRow, 4 + signum(deltaC)), Piece(movingPiece.color, Rook))
      } else gameState
    }
  }

  def updateEnPassantTarget(movingPiecePos: Position, movingPiece: Piece, destinationPos: Position)(
      gameState: GameState
  ): GameState = {
    if (movingPiece.pieceType == Pawn && abs(destinationPos.row - movingPiecePos.row) == 2) {
      gameState.updateEnPassantTarget(
        Position((movingPiecePos.row + destinationPos.row) / 2, movingPiecePos.col)
      )
    } else gameState
  }

  def updateCastleStatus(movingPiece: Piece, movingPiecePos: Position)(
      gameState: GameState
  ): GameState = {
    movingPiece.pieceType match {
      case Rook =>
        movingPiece.color match {
          case White =>
            if (gameState.castleStatus.whiteQueen && movingPiecePos == Position(7, 0)) {
              gameState.updateCastleStatus(Piece(White, Queen), false)
            } else if (gameState.castleStatus.whiteKing && movingPiecePos == Position(7, 7)) {
              gameState.updateCastleStatus(Piece(White, King), false)
            } else gameState
          case Black =>
            if (gameState.castleStatus.blackQueen && movingPiecePos == Position(0, 0)) {
              gameState.updateCastleStatus(Piece(Black, Queen), false)
            } else if (gameState.castleStatus.blackKing && movingPiecePos == Position(0, 7)) {
              gameState.updateCastleStatus(Piece(Black, King), false)
            } else gameState
        }
      case _ => gameState
    }
  }

  def updatePiecePosition(movingPiecePos: Position, movingPiece: Piece, destinationPos: Position)(
      gameState: GameState
  ): GameState = {
    gameState.updateSquare(movingPiecePos, Blank).updateSquare(destinationPos, movingPiece)
  }
}
