package models.rules

import models.actions.UpdateGameState.updateGameState
import models.rules.Check.isCurrentPlayerInCheck
import models.rules.ValidMoves._
import models.utils.DataTypes._
import models.utils.Pieces._
import scala.collection.parallel.ParSeq
import test.framework.UnitSpec

class ValidMovesTest extends UnitSpec {

  behavior of "Rays"

  it should "not contain any pieces of same color" in {
    repeat(10) {
      val gameState = getGameState()
      val color = getColor()
      val pos = getPosition()
      val deltas = List(
        Position(-1, -1),
        Position(-1, 0),
        Position(-1, 1),
        Position(0, -1),
        Position(0, 1),
        Position(1, -1),
        Position(1, 0),
        Position(1, 1)
      )

      for (delta <- deltas) {
        getRaySquares(gameState, pos, delta, color)
          .filter({
            case p: Piece => p.color == color
            case _        => false
          }) shouldBe empty
      }
    }
  }

  it should "contain opponent pieces only in last position" in {
    repeat(10) {
      val gameState = getGameState()
      val color = getColor()
      val opponentColor = if (color == White) Black else White
      val pos = getPosition()
      val deltas = List(
        Position(-1, -1),
        Position(-1, 0),
        Position(-1, 1),
        Position(0, -1),
        Position(0, 1),
        Position(1, -1),
        Position(1, 0),
        Position(1, 1)
      )

      for (delta <- deltas) {
        val raySquares = getRaySquares(gameState, pos, delta, color)

        if (!raySquares.isEmpty) {
          val nonBlankTail = raySquares.dropWhile(_.isBlank)

          nonBlankTail.length should be <= 1

          List(
            Blank,
            Piece(opponentColor, Pawn),
            Piece(opponentColor, Knight),
            Piece(opponentColor, Bishop),
            Piece(opponentColor, Rook),
            Piece(opponentColor, Queen),
            Piece(opponentColor, King)
          ) should contain(raySquares.last)
        }
      }

    }
  }

  behavior of "En passant target"

  it should "be present in pawn move at capture position" in {
    val gameState = GameState("8/8/8/1Pp5/8/8/8/4K3 w - c6 0 1")

    allPossibleMoves(gameState, Position(3, 1)) should contain theSameElementsAs List(
      Position(2, 2),
      Position(2, 1)
    )
    getLegalMoves(gameState, Position(3, 1)) should contain(Position(2, 2))
  }

  it should "not be present in pawn moves if not in capture position" in {
    val gameState = GameState("8/8/8/2p5/8/8/3P4/4K3 w - c6 0 1")

    allPossibleMoves(gameState, Position(6, 3)) shouldNot contain(Position(2, 2))
  }

  behavior of "Check"

  // Still worth hard-coding check since it is faster.
  it should "be equivalent to getAttackingPieces != Nil" in {
    repeat(1000) {
      val gameState = getRealisticGameState()
      val oppColor = if (gameState.whoseMove == White) Black else White

      !getAttackingPieces(gameState, oppColor).isEmpty should equal(
        isCurrentPlayerInCheck(gameState)
      )
    }
  }

  it should "disable other moves" in {
    val gameState = GameState("8/8/8/8/8/8/P7/4K2r w - - 0 1")

    allPossibleMoves(gameState, Position(6, 0)) should contain theSameElementsAs List(
      Position(5, 0),
      Position(4, 0)
    )
    getLegalMoves(gameState, Position(6, 0)) shouldBe empty
  }

  it should "not be possible for player to put king in check" in {
    val gameState = GameState("8/8/8/8/8/8/6r1/3K4 w - - 0 1")

    allPossibleMoves(gameState, Position(7, 3)).filter(p =>
      p.row == 6
    ) should contain theSameElementsAs List(
      Position(6, 2),
      Position(6, 3),
      Position(6, 4)
    )
    getLegalMoves(gameState, Position(7, 3)).filter(p => p.row == 6) shouldBe empty
  }

  it should "not be possible for player to expose king to attack" in {
    val gameState = GameState("8/8/8/8/6b1/8/4P3/3K4 w - - 0 1")

    allPossibleMoves(gameState, Position(6, 4)) should contain theSameElementsAs List(
      Position(5, 4),
      Position(4, 4)
    )
    getLegalMoves(gameState, Position(6, 4)) shouldBe empty
  }

  it should "not be possible for player to en passant if it puts self in check" in {
    val gameState = GameState("8/8/8/KPp4r/8/8/8/8 w - c6 0 1")

    allPossibleMoves(gameState, Position(3, 1)) should contain theSameElementsAs List(
      Position(2, 1),
      Position(2, 2)
    )
    getLegalMoves(gameState, Position(3, 1)) shouldNot contain(Position(2, 2))
  }

  behavior of "Castling"

  it should "be possible if there are no obstructions" in {
    val gameState = GameState("8/8/8/8/8/8/8/R3K2R w KQ - 0 1")

    getLegalMoves(gameState, Position(7, 4)) should contain allOf (Position(7, 2), Position(7, 6))
  }

  it should "not be possible if pieces are in the way" in {
    val gameState1 = GameState("8/8/8/8/8/8/8/R3K1NR w KQ - 0 1")
    val gameState2 = GameState("8/8/8/8/8/8/8/R1B1K2R w KQ - 0 1")

    getLegalMoves(gameState1, Position(7, 4)) should contain(Position(7, 2))
    getLegalMoves(gameState1, Position(7, 4)) shouldNot contain(Position(7, 6))
    getLegalMoves(gameState2, Position(7, 4)) shouldNot contain(Position(7, 2))
    getLegalMoves(gameState2, Position(7, 4)) should contain(Position(7, 6))
  }

  it should "not be possible if the king is in check" in {
    val gameState = GameState("4r4/8/8/8/8/8/8/R3K2R w KQ - 0 1")

    getLegalMoves(gameState, Position(7, 4)) should contain noneOf (Position(7, 2), Position(7, 6))
  }

  behavior of "Valid moves generation"

  /** E2E valid moves count test (comparing to stockfish) */
  it should "generate the same move counts as stockfish" in {
    val gameState = GameState("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8")
    val results = (1 to 5).map(i => {
      println(s"Testing valid move generations, ${i} ply...")
      getNumberOfMoveSequences(i, gameState, false)
    })

    results should contain theSameElementsAs List(44, 1486, 62379, 2103487, 89941194)
  }

  /* Utility functions */

  def getNumberOfMoveSequences(depth: Int, gameState: GameState, doPrint: Boolean): Long = {
    if (depth == 0) return 1

    val moves = getAllLegalMoves(gameState)

    if (moves.isEmpty) 0
    else
      ParSeq
        .fromSpecific(moves)
        .map(move =>
          move match {
            case Move(startPos, endPos, pieceType) => {
              val updatedGameState = updateGameState(gameState, move)

              val res = getNumberOfMoveSequences(depth - 1, updatedGameState, false)

              if (doPrint)
                println(s"${startPos.toFileRank()}${endPos.toFileRank()}${if (pieceType == null) ""
                else pieceType.toString()}\tResult: ${res}")

              res
            }
            case NoMove => throw new Exception("getAllLegalMoves returned nonsense")
          }
        )
        .reduce(_ + _)
  }

  private def getRealisticGameState(): GameState = {
    val whiteKingPosition = getPosition()
    var blackKingPosition = getPosition()
    // make sure these are different
    while (blackKingPosition == whiteKingPosition) {
      blackKingPosition = getPosition()
    }

    // Make sure game only has one king of each type
    getGameState(skipKings = true)
      .updateSquare(whiteKingPosition, Piece(White, King))
      .updateSquare(blackKingPosition, Piece(Black, King))
      .updateEnPassantTarget(null)
      .updateCastleStatus(Piece(White, King), false)
      .updateCastleStatus(Piece(White, Queen), false)
      .updateCastleStatus(Piece(Black, King), false)
      .updateCastleStatus(Piece(Black, Queen), false)
  }

}
