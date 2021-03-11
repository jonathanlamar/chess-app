package models.utils

import models.utils.Fen._

object DataTypes {

  sealed trait Color

  final object White extends Color
  final object Black extends Color

  case class Player(color: Color)

  sealed trait PieceType

  final object King extends PieceType
  final object Queen extends PieceType
  final object Bishop extends PieceType
  final object Knight extends PieceType
  final object Rook extends PieceType
  final object Pawn extends PieceType

  sealed trait Square {
    val color: Color
    def isBlank: Boolean
  }

  final object Blank extends Square {
    override def toString(): String = " "
    def isBlank: Boolean = true
    val color = null
  }

  final case class Piece(color: Color, pieceType: PieceType) extends Square {
    override def toString(): String = {
      val letter = pieceType match {
        case Pawn   => "P"
        case Knight => "N"
        case Bishop => "B"
        case Rook   => "R"
        case Queen  => "Q"
        case King   => "K"
      }

      color match {
        case Black => letter.toLowerCase()
        case White => letter
      }
    }

    def isBlank: Boolean = false
  }

  object Piece {
    def apply(fenPiece: Char): Piece = {
      fenPiece match {
        case 'p' => Piece(Black, Pawn)
        case 'r' => Piece(Black, Rook)
        case 'n' => Piece(Black, Knight)
        case 'b' => Piece(Black, Bishop)
        case 'q' => Piece(Black, Queen)
        case 'k' => Piece(Black, King)
        case 'P' => Piece(White, Pawn)
        case 'R' => Piece(White, Rook)
        case 'N' => Piece(White, Knight)
        case 'B' => Piece(White, Bishop)
        case 'Q' => Piece(White, Queen)
        case 'K' => Piece(White, King)
        case _   => throw new Exception(s"Invalid character to construct Piece: ${fenPiece}")
      }
    }
  }

  case class Position(row: Int, col: Int) {
    def toFileRank(): String = {
      "abcdefgh".substring(row, row + 1) + (8 - col).toString()
    }

    // For computing valid squares
    def +(other: Position): Position = Position(row + other.row, col + other.col)
    def *(other: Int): Position = Position(row * other, col * other)
    def isInBounds: Boolean = row >= 0 && row < 8 && col >= 0 && col < 8
    def verticalFlip: Position = Position(-row, col)
  }

  object Position {
    def apply(fileRank: String): Position = {
      val r = "abcdefgh".indexOf(fileRank.substring(0, 1))
      val c = 8 - fileRank.substring(1).toInt

      Position(r, c)
    }
  }

  case class CastleStatus(
      blackKing: Boolean,
      blackQueen: Boolean,
      whiteKing: Boolean,
      whiteQueen: Boolean
  ) {
    override def toString(): String = {
      List('K', 'Q', 'k', 'q')
        .zip(List(whiteKing, whiteQueen, blackKing, blackQueen))
        .filter({ case (c: Char, v: Boolean) => v })
        .map({ case (c: Char, v: Boolean) => c })
        .mkString
    }
  }

  case class Board(
      squares: List[List[Square]],
      whoseMove: Color,
      castleStatus: CastleStatus,
      enPassantTarget: Position,
      halfMoveClock: Int,
      fullMoveCount: Int
  ) {
    override def toString(): String = {
      squares.map(_.mkString + "\n").mkString
    }
  }

  object Board {
    def apply(fenString: String): Board = parseFenString(fenString)
  }
}
