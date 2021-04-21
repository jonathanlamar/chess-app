package models.ai.minimax

import models.utils.DataTypes._
import scala.util.Random.nextLong

class Zobrist {
  private val pieceMap = Map(
    Piece(Black, Pawn) -> 0,
    Piece(Black, Knight) -> 1,
    Piece(Black, Bishop) -> 2,
    Piece(Black, Rook) -> 3,
    Piece(Black, Queen) -> 4,
    Piece(Black, King) -> 5,
    Piece(White, Pawn) -> 6,
    Piece(White, Knight) -> 7,
    Piece(White, Bishop) -> 8,
    Piece(White, Rook) -> 9,
    Piece(White, Queen) -> 10,
    Piece(White, King) -> 11
  )

  private val table = List.fill(8, 8, 12)(nextLong())

  def computeHash(gameState: GameState): Long = {
    var hash: Long = 0
    for (r <- 0 until 8; c <- 0 until 8) {
      gameState.squares(r)(c) match {
        case piece: Piece => hash = hash ^ table(r)(c)(pieceMap(piece))
        case Blank        => ()
      }
    }

    hash
  }

  def updateHash(gameState: GameState, move: MoveLike, hashVal: Long): Long = {
    move match {
      case m: Move => {
        var newHashVal: Long = hashVal

        gameState.squares(m.from) match {
          case piece: Piece => {
            newHashVal = newHashVal ^ table(m.from.row)(m.from.col)(pieceMap(piece))
            gameState.squares(m.to) match {
              case piece: Piece =>
                newHashVal = newHashVal ^ table(m.to.row)(m.to.col)(pieceMap(piece))
              case Blank => ()
            }
            newHashVal = newHashVal ^ table(m.from.row)(m.from.col)(pieceMap(piece))
          }
          case Blank => ()
        }

        newHashVal
      }
      case NoMove => hashVal
    }
  }
}
