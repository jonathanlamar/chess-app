package models.rules

import models.actions.UpdateGameState.updateGameState
import models.rules.ValidMoves.{allPossibleMoves, getLegalMoves, getRaySquares}
import models.utils.DataTypes._
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

  /** Cannot en passant if it puts self in check. */
  it should "not be possible for player to en passant if it puts self in check" in {
    val gameState = GameState("8/8/8/KPp4r/8/8/8/8 w - c6 0 1")

    allPossibleMoves(gameState, Position(3, 1)) should contain theSameElementsAs List(
      Position(2, 1),
      Position(2, 2)
    )
    getLegalMoves(gameState, Position(3, 1)) shouldNot contain(Position(2, 2))
  }

  // behavior of "Valid moves generation"

  // /** E2E valid moves count test (comparing to stockfish) */
  // it should "generate the same move counts as stockfish" in {
  //   val gameState = GameState("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8")
  //   val results = (1 to 5).map(i => getNumberOfMoveSequences(i, gameState, true))

  //   results should contain theSameElementsAs List(44, 1486, 62379, 2103487, 89941194)
  // }

  /* Utility functions */

  private def getNumberOfMoveSequences(depth: Int, gameState: GameState, doPrint: Boolean): Long = {
    if (depth == 0) return 1

    val moves = getAllLegalMoves(gameState)
    var numPositions: Long = 0

    for ((startPos, endPos, pieceType) <- moves) {
      val updatedGameState = updateGameState(gameState, startPos, endPos, pieceType)
      numPositions += getNumberOfMoveSequences(depth - 1, updatedGameState, false)
    }

    if (doPrint) println(s"Depth: ${depth} ply\tResult: ${numPositions} positions")

    return numPositions
  }

  private def getAllLegalMoves(gameState: GameState): List[(Position, Position, PieceType)] = {
    val pieceMoves = gameState.piecesIndex.view
      .filterKeys(_.color == gameState.whoseMove)
      .values
      .flatten
      .flatMap(pos => getLegalMoves(gameState, pos).map((pos, _)))
      .toList

    def addPawnPromotion(move: (Position, Position)): List[(Position, Position, PieceType)] = {
      gameState.squares(move._1.row)(move._1.col) match {
        case Blank => throw new Exception("Cannot move blank square")
        case Piece(color, Pawn) =>
          if ((color == Black && move._2.row == 7) || (color == White && move._2.row == 0)) {
            List(Rook, Knight, Bishop, Queen).map((move._1, move._2, _))
          } else {
            List((move._1, move._2, null))
          }
        case _: Piece => List((move._1, move._2, null))
      }
    }

    pieceMoves.flatMap(addPawnPromotion)
  }

}
