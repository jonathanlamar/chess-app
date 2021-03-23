package test

import models.utils.DataTypes._
import scala.util.Random.{nextBoolean, nextInt}

/** Generates random instances of datatypes for unit testing.
  * These are not all uniform, but I don't think they need to be.  Plus, that
  * would have resulted in more complex code, which I didn't want to write.
  */
object TestData {
  def getColor(): Color = if (nextBoolean()) Black else White

  def getPieceType(): PieceType = nextElement(List(Pawn, Knight, Bishop, Rook, Queen, King))

  /** 50% chance of being blank.  Otherwise uniformly random Piece */
  def getSquare(): Square = if (nextBoolean()) Blank else getPiece()

  def getPiece(): Piece = Piece(getColor(), getPieceType())

  def getPosition(): Position = Position(nextInt(8), nextInt(8))

  def getCastleStatus(): CastleStatus = CastleStatus(
    blackKing = nextBoolean(),
    blackQueen = nextBoolean(),
    whiteKing = nextBoolean(),
    whiteQueen = nextBoolean()
  )

  def getGameState(): GameState = GameState(
    squares = List.fill(8, 8)(getSquare()),
    whoseMove = getColor(),
    castleStatus = getCastleStatus(),
    enPassantTarget = getPosition(),
    halfMoveClock = nextInt(50),
    fullMoveCount = nextInt(75)
  )

  /** Generated independently of getGameState */
  def getFenString(): String = {
    val fenBoard = List.fill(8)(getFenRow()).reduce(_ + "/" + _)
    val whoseTurn = if (nextBoolean()) "b" else "w"
    val castleStatus = {
      val castlePieces = List(
        ('K', nextBoolean()),
        ('Q', nextBoolean()),
        ('k', nextBoolean()),
        ('q', nextBoolean())
      ).filter(_._2).map(_._1)

      if (!castlePieces.isEmpty) castlePieces.mkString else "-"
    }
    val enPassantTarget = if (nextBoolean()) "-" else getFileRank()
    val halfMoveClock = nextInt(50).toString()
    val fullMoveCount = nextInt(75).toString()

    fenBoard + " " +
      whoseTurn + " " +
      castleStatus + " " +
      enPassantTarget + " " +
      halfMoveClock + " " +
      fullMoveCount
  }

  /** 50% chance of check, 25% chance of checkmate */
  def getCheckStatus(): CheckStatus = {
    val check = nextBoolean()
    val checkmate = if (check) nextBoolean() else false

    CheckStatus(check, checkmate)
  }

  private def getFenRow(): String = {
    val squaresAndVals = List(
      '1', '2', '3', '4', '5', '6', '7', '8', 'p', 'r', 'n', 'b', 'q', 'k', 'P', 'R', 'N', 'B', 'Q',
      'K'
    )

    def buildString(s: String, n: Int): (String, Int) = {
      if (n >= 8) (s, n)
      else {
        val c =
          if (!s.isEmpty && s.last.isDigit) nextElement(squaresAndVals.filter(!_.isDigit))
          else nextElement(squaresAndVals)
        val m = if (c.isDigit) c.asDigit else 1

        buildString(s.appended(c), n + m)
      }
    }

    buildString("", 0)._1
  }

  private def getFileRank(): String = {
    nextElement(List("a", "b", "c", "d", "e", "f", "g", "h")) +
      nextElement(List("1", "2", "3", "4", "5", "6", "7", "8"))
  }

  private def nextElement[A](ls: List[A]): A = ls match {
    case Nil => throw new Exception("Cannot return element of empty list")
    case _   => ls(nextInt(ls.length))
  }
}
