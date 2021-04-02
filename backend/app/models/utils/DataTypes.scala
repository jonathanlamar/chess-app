package models.utils

import models.utils.Fen._

/** Data model for chess pieces, positions, and boards. */
object DataTypes {

  sealed trait Color {
    def reverse: Color
  }

  final object White extends Color {
    override def toString(): String = "W"
    override def reverse: Color = Black
  }
  final object Black extends Color {
    override def toString(): String = "B"
    override def reverse: Color = White
  }

  sealed trait PieceType

  final object King extends PieceType {
    override def toString(): String = "K"
  }
  final object Queen extends PieceType {
    override def toString(): String = "Q"
  }
  final object Bishop extends PieceType {
    override def toString(): String = "B"
  }
  final object Knight extends PieceType {
    override def toString(): String = "N"
  }
  final object Rook extends PieceType {
    override def toString(): String = "R"
  }
  final object Pawn extends PieceType {
    override def toString(): String = "P"
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
    def -(other: Position): Position = Position(row - other.row, col - other.col)
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

  sealed trait MoveLike
  final object NoMove extends MoveLike {
    override def toString(): String = "NO MOVE"
  }
  final case class Move(from: Position, to: Position, promotePawnType: PieceType = null)
      extends MoveLike {
    override def toString(): String =
      if (promotePawnType == null) from.toFileRank() + to.toFileRank()
      else from.toFileRank() + to.toFileRank() + promotePawnType.toString()
  }

  object Move {
    val promoteTypeMap: Map[Char, PieceType] = Map(
      ('p', Pawn),
      ('n', Knight),
      ('b', Bishop),
      ('r', Rook),
      ('q', Queen),
      ('k', King),
      ('P', Pawn),
      ('N', Knight),
      ('B', Bishop),
      ('R', Rook),
      ('Q', Queen),
      ('K', King)
    )

    def apply(fileRankMove: String): Move = {
      if ("""^[a-h][1-8][a-h][1-8]$""".r.matches(fileRankMove)) {
        Move(Position(fileRankMove.substring(0, 2)), Position(fileRankMove.substring(2, 4)))
      } else if ("""^[a-h][1-8][a-h][1-8][pPnNbBrRqQkK]$""".r.matches(fileRankMove)) {
        val promoteType = promoteTypeMap(fileRankMove.charAt(4))

        Move(
          Position(fileRankMove.substring(0, 2)),
          Position(fileRankMove.substring(2, 4)),
          promoteType
        )
      } else throw new Exception("Cannot parse fileRankMove string")
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

  case class GameState(
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
      squares
        .map(row => row.foldLeft("+---" * 8 + "+\n|")((str, s) => str + " " + s.toString() + " |"))
        .zipWithIndex
        .map({ case (s: String, i: Int) => s + " " + (8 - i).toString() + "\n" })
        .mkString + "+---" * 8 + "+\n  a   b   c   d   e   f   g   h"
    }

    def transform(f: GameState => GameState): GameState = f(this)

    lazy val piecesIndex: Map[Piece, List[Position]] = {
      (for (r <- 0 until 8; c <- 0 until 8 if !squares(r)(c).isBlank)
        yield squares(r)(c).asInstanceOf[Piece] -> Position(r, c)).toList
        .groupBy(_._1)
        .view
        .mapValues(_.map(_._2))
        .toMap
    }

    def squares(pos: Position): Square = squares(pos.row)(pos.col)

    // TODO: These are ugly.  Should really use builder pattern here.
    def updateSquare(pos: Position, square: Square): GameState = {
      val squares =
        for (r <- 0 until 8) yield {
          for (c <- 0 until 8) yield {
            if (r == pos.row && c == pos.col) square else this.squares(r)(c)
          }
        }

      GameState(
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
    def updateWhoseMove(whoseMove: Color): GameState =
      GameState(
        squares = this.squares,
        whoseMove = whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def updateCastleStatus(piece: Piece, status: Boolean): GameState = {
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
          blackQueen = this.castleStatus.blackQueen,
          blackKing = this.castleStatus.blackKing,
          whiteQueen = status,
          whiteKing = this.castleStatus.whiteKing
        )
      } else if (piece.color == White && piece.pieceType == King) {
        CastleStatus(
          blackQueen = this.castleStatus.blackQueen,
          blackKing = this.castleStatus.blackKing,
          whiteQueen = this.castleStatus.whiteQueen,
          whiteKing = status
        )
      } else throw new Exception("Wrong piece type for castle status update.")

      GameState(
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

    def updateEnPassantTarget(enPassantTarget: Position): GameState =
      GameState(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def updateHalfMoveClock(halfMoveClock: Int): GameState =
      GameState(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def updateFullMoveCount(fullMoveCount: Int): GameState =
      GameState(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = fullMoveCount,
        whiteCapturedPieces = this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def addWhiteCapturedPiece(piece: Piece): GameState =
      GameState(
        squares = this.squares,
        whoseMove = this.whoseMove,
        castleStatus = this.castleStatus,
        enPassantTarget = this.enPassantTarget,
        halfMoveClock = this.halfMoveClock,
        fullMoveCount = this.fullMoveCount,
        whiteCapturedPieces = piece :: this.whiteCapturedPieces,
        blackCapturedPieces = this.blackCapturedPieces
      )
    def addBlackCapturedPiece(piece: Piece): GameState =
      GameState(
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

  object GameState {
    def apply(fenString: String): GameState = parseFenString(fenString)
  }

  case class CheckStatus(isInCheck: Boolean, isInCheckmate: Boolean)
}
