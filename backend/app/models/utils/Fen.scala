package models.utils

import models.utils.DataTypes._

/** Logic for decoding and encoding FEN strings, which are defined as:
  *
  * https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
  *
  * Each rank is described, starting with rank 8 and ending with rank 1;
  * within each rank, the contents of each square are described from file "a"
  * through file "h". Following the Standard Algebraic Notation (SAN), each
  * piece is identified by a single letter taken from the standard English
  * names (pawn = "P", knight = "N", bishop = "B", rook = "R", queen = "Q" and
  * king = "K"). White pieces are designated using upper-case letters
  * ("PNBRQK") while black pieces use lowercase ("pnbrqk"). Empty squares are
  * noted using digits 1 through 8 (the number of empty squares), and "/"
  * separates ranks.
  *
  * Here's the FEN for the starting position:
  *
  * Here's the FEN for the starting position:
  *
  * rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
  * And after the move 1.e4:
  *
  * rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1
  * And then after 1...c5:
  *
  * rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2
  * And then after 2.Nf3:
  *
  * rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2
  */
object Fen {

  // TODO: Write logic for encoding FEN strings.

  val fenRegex =
    """\s*^(((?:[rnbqkpRNBQKP1-8]+\/){7})[rnbqkpRNBQKP1-8]+)\s([b|w])\s([K|Q|k|q]{1,4})\s(-|[a-h][1-8])\s(\d+\s\d+)$""".r

  val zeroCharValue = 48 // Is this universal?

  def parseFenString(fenString: String): Board = {
    // TODO - Functional exception handling
    // FIXME - this regex is broken
    // if (!fenRegex.matches(fenString)) throw new Exception("Not a valid FEN string.")

    val fenParts = fenString.split(" ")

    val squares = parseFenBoardRep(fenParts(0))
    val whoseMove = if (fenParts(1) == "w") White else Black
    val castleStatus = getCastleStatus(fenParts(2))
    val enPassantTarget = if (fenParts(3) == "-") null else Position(fenParts(3))
    val halfMoveClock = fenParts(4).toInt
    val fullMoveCount = fenParts(5).toInt

    Board(squares, whoseMove, castleStatus, enPassantTarget, halfMoveClock, fullMoveCount)
  }

  def parseFenBoardRep(boardRep: String): List[List[Square]] = {
    boardRep.split("/").map(processFenRow).map(parseProcessedFenRow).toList
  }

  def processFenRow(fenRow: String): String = {
    fenRow.foldLeft("")((x, y) => {
      if (y.isDigit) x + "1" * (y - zeroCharValue)
      else x + y.toString()
    })
  }

  def parseProcessedFenRow(processedFenRow: String): List[Square] = {
    processedFenRow.map(c => if (c == '1') Blank else Piece(c)).toList
  }

  def getCastleStatus(castleFenPart: String): CastleStatus = CastleStatus(
    castleFenPart.indexOf("k") != -1,
    castleFenPart.indexOf("q") != -1,
    castleFenPart.indexOf("K") != -1,
    castleFenPart.indexOf("Q") != -1
  )

  def toFenString(board: Board): String = {
    val fenBoard = toFenBoard(board.squares)
    val whoseMove = board.whoseMove match {
      case Black => "b"
      case White => "w"
    }
    val castleStatus = toFenCastleStatus(board.castleStatus)
    val enPassantTarget =
      if (board.enPassantTarget != null) board.enPassantTarget.toFileRank() else "-"
    val halfMoveClock = board.halfMoveClock.toString()
    val fullMoveCount = board.fullMoveCount.toString()

    return List(
      fenBoard,
      whoseMove,
      castleStatus,
      enPassantTarget,
      halfMoveClock,
      fullMoveCount
    ).reduce(_ + " " + _)
  }

  def toFenBoard(squares: List[List[Square]]): String = {
    squares.map(toProcessedFenRow).map(compressProcessedFenRow).reduce(_ + "/" + _)
  }

  def toProcessedFenRow(row: List[Square]): String = {
    row.map(toFenPiece).mkString
  }

  def toFenPiece(square: Square): Char = {
    square match {
      case Blank => '1'
      case Piece(color, pieceType) => {
        val c = pieceType match {
          case Pawn   => 'P'
          case Knight => 'N'
          case Bishop => 'B'
          case Rook   => 'R'
          case Queen  => 'Q'
          case King   => 'K'
        }

        color match {
          case Black => c.toLower
          case White => c
        }
      }
    }
  }

  def compressProcessedFenRow(processedFenRow: String): String = {
    if (!processedFenRow.contains("11")) processedFenRow
    else {
      val firstDoubleOneIdx = processedFenRow.indexOf("11")
      val prefix = processedFenRow.take(firstDoubleOneIdx)
      val onesBlock = processedFenRow.drop(firstDoubleOneIdx).takeWhile(_ == '1')
      val numOnes = onesBlock.length
      val numOnesChar = numOnes.toString()
      val suffix = processedFenRow.drop(firstDoubleOneIdx + numOnes)
      val processedString = prefix + numOnesChar + suffix

      compressProcessedFenRow(processedString)
    }
  }

  def toFenCastleStatus(castleStatus: CastleStatus): String = {
    List(
      castleStatus.whiteKing,
      castleStatus.whiteQueen,
      castleStatus.blackKing,
      castleStatus.blackQueen
    )
      .zip(List("K", "Q", "k", "q"))
      .filter(_._1)
      .map(_._2)
      .reduce(_ + _)
  }
}
