package models.utils

import models.utils.DataTypes._

/** https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
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

  val fenRegex =
    """\s*^(((?:[rnbqkpRNBQKP1-8]+\/){7})[rnbqkpRNBQKP1-8]+)\s([b|w])\s([K|Q|k|q]{1,4})\s(-|[a-h][1-8])\s(\d+\s\d+)$""".r

  def parseFenString(fenString: String): Board = {
    // TODO - Functional exception handling
    if (!fenRegex.matches(fenString)) throw new Exception("Not a valid FEN string.")

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
    val zeroCharValue = 48 // Is this universal?

    fenRow.foldLeft("")((x, y) => {
      if (y.isDigit) x + "1" + (y - zeroCharValue)
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
}