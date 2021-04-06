package models.ai.minimax

import models.utils.DataTypes._
import scala.util.Random.nextLong

object Zobrist {
  val pieceMap = Map(
    Piece(Black, Pawn) -> 0,
    Piece(Black, Knight) -> 0,
    Piece(Black, Bishop) -> 0,
    Piece(Black, Rook) -> 0,
    Piece(Black, Queen) -> 0,
    Piece(Black, King) -> 0,
    Piece(White, Pawn) -> 0,
    Piece(White, Knight) -> 0,
    Piece(White, Bishop) -> 0,
    Piece(White, Rook) -> 0,
    Piece(White, Queen) -> 0,
    Piece(White, King) -> 0
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
}
