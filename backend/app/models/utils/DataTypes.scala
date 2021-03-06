package models.utils

import models.utils.Fen._

/** Data model for chess pieces, positions, and boards. */
object DataTypes {

  sealed trait Color

  final object White extends Color {
    override def toString(): String = "White"
  }
  final object Black extends Color {
    override def toString(): String = "Black"
  }

  case class Player(color: Color)

  sealed trait PieceType

  final object King extends PieceType {
    override def toString(): String = "King"
  }
  final object Queen extends PieceType {
    override def toString(): String = "Queen"
  }
  final object Bishop extends PieceType {
    override def toString(): String = "Bishop"
  }
  final object Knight extends PieceType {
    override def toString(): String = "Knight"
  }
  final object Rook extends PieceType {
    override def toString(): String = "Rook"
  }
  final object Pawn extends PieceType {
    override def toString(): String = "Pawn"
  }

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
      "abcdefgh".substring(col, col + 1) + (8 - row).toString()
    }

    // For computing valid squares
    def +(other: Position): Position = Position(row + other.row, col + other.col)
    def *(other: Int): Position = Position(row * other, col * other)
    def isInBounds: Boolean = row >= 0 && row < 8 && col >= 0 && col < 8
    def verticalFlip: Position = Position(-row, col)
  }

  object Position {
    def apply(fileRank: String): Position = {
      val r = 8 - fileRank.substring(1).toInt
      val c = "abcdefgh".indexOf(fileRank.substring(0, 1))

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
      fullMoveCount: Int,
      whiteCapturedPieces: List[Piece] = Nil,
      blackCapturedPieces: List[Piece] = Nil
  ) {
    override def toString(): String = {
      squares.map(_.mkString + "\n").mkString
    }

    def transform(f: Board => Board): Board = f(this)

    // TODO: These are ugly.  Should really use builder pattern here.
    def updateSquare(pos: Position, square: Square): Board = {
      val squares =
        for (r <- 0 until 8) yield {
          for (c <- 0 until 8) yield {
            if (r == pos.row && c == pos.col) square else this.squares(r)(c)
          }
        }

      Board(
        squares = squares.map(_.toList).toList,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    }
    def updateWhoseMove(whoseMove: Color): Board =
      Board(
        squares = this.squares,
        whoseMove = whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def updateCastleStatus(piece: Piece, status: Boolean): Board = {
      val castleStatus = if (piece.color == Black && piece.pieceType == Queen) {
        CastleStatus(
          blackQueen = status,
          blackKing = this.castleStatus.blackKing,
          whiteQueen = this.castleStatus.whiteQueen,
          whiteKing = this.castleStatus.whiteKing
        )
      } else if (piece.color == Black && piece.pieceType == King) {
        CastleStatus(
          blackQueen = this.castleStatus.blackQueen,
          blackKing = status,
          whiteQueen = this.castleStatus.whiteQueen,
          whiteKing = this.castleStatus.whiteKing
        )
      } else if (piece.color == White && piece.pieceType == Queen) {
        CastleStatus(
          blackQueen = status,
          blackKing = this.castleStatus.blackKing,
          whiteQueen = this.castleStatus.whiteQueen,
          whiteKing = this.castleStatus.whiteKing
        )
      } else if (piece.color == White && piece.pieceType == King) {
        CastleStatus(
          blackQueen = status,
          blackKing = this.castleStatus.blackKing,
          whiteQueen = this.castleStatus.whiteQueen,
          whiteKing = this.castleStatus.whiteKing
        )
      } else throw new Exception("Wrong piece type for castle status update.")

      Board(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    }

    def updateEnPassantTarget(enPassantTarget: Position): Board =
      Board(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def updateHalfMoveClock(halfMoveClock: Int): Board =
      Board(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def updateFullMoveCount(fullMoveCount: Int): Board =
      Board(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def addWhiteCapturedPiece(piece: Piece): Board =
      Board(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = piece :: this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def addBlackCapturedPiece(piece: Piece): Board =
      Board(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = piece :: this.blackCapturedPieces
      )
  }

  object Board {
    def apply(fenString: String): Board = parseFenString(fenString)
  }
}
