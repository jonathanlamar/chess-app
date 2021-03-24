package models.rules

import models.rules.ValidMoves._
import models.utils.DataTypes._
import test.framework.UnitSpec

class ValidMovesTest extends UnitSpec {

  // TODO: All of the asserts for restriction of legal moves should also have a
  // corresponding assert about all possible moves.  That way we know the check
  // logic is really limiting moves.

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

  behavior of "Valid moves generation"

  /** E2E valid moves count test (comparing to stockfish) */
  it should "generate the same move counts as stockfish" in {
    // TODO
  }

}
