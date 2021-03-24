package models.actions

import models.actions.UpdateGameState._
import models.utils.DataTypes._
import scala.util.Random.nextInt
import test.framework.UnitSpec

class UpdateGameStateTest extends UnitSpec {

  behavior of "En passant update"

  it should "populate the target after a double pawn push" in {
    val gameState = GameState("8/pppppppp/8/8/8/8/PPPPPPPP/8 w - - 0 1")
    val whitePawnPos = Position(6, nextInt(8))
    val blackPawnPos = Position(1, nextInt(8))
    val updatedGameState1 = updateGameState(gameState, whitePawnPos, whitePawnPos + Position(-2, 0))
    val updatedGameState2 =
      updateGameState(updatedGameState1, blackPawnPos, blackPawnPos + Position(2, 0))

    updatedGameState1.enPassantTarget should equal(whitePawnPos + Position(-1, 0))
    updatedGameState2.enPassantTarget should equal(blackPawnPos + Position(1, 0))
  }

  it should "remove the previous double-pushed pawn" in {
    val gameState = GameState("8/3p4/8/2P5/8/8/8/8 b - - 0 1")
    val updatedGameState1 = updateGameState(gameState, Position(1, 3), Position(3, 3))
    val updatedGameState2 = updateGameState(updatedGameState1, Position(3, 2), Position(2, 3))

    updatedGameState2.squares(3)(3) should be(Blank)
  }

  behavior of "Castling"

  it should "be disabled after a king move" in {
    val gameState = GameState("4k3/8/8/8/8/8/8/4K3 b KQkq - 0 1")
    val updatedGameState1 = updateGameState(gameState, Position(0, 4), Position(1, 4))
    val updatedGameState2 = updateGameState(updatedGameState1, Position(7, 4), Position(7, 5))

    updatedGameState1.castleStatus should equal(CastleStatus(false, false, true, true))
    updatedGameState2.castleStatus should equal(CastleStatus(false, false, false, false))
  }

  it should "be disabled even if king moves back to original position" in {
    val gameState = GameState("4k3/8/8/8/8/8/2P5/8 b KQkq - 0 1")
    val updatedGameState1 = updateGameState(gameState, Position(0, 4), Position(1, 4))
    val updatedGameState2 = updateGameState(updatedGameState1, Position(6, 2), Position(5, 2))
    val updatedGameState3 = updateGameState(updatedGameState2, Position(1, 4), Position(0, 4))

    updatedGameState3.castleStatus should equal(CastleStatus(false, false, true, true))
  }

  it should "be disabled for rook after rook moves" in {
    val gameState = GameState("8/8/8/8/8/8/8/4K2R w KQkq - 0 1")
    val updatedGameState1 = updateGameState(gameState, Position(7, 7), Position(6, 7))

    updatedGameState1.castleStatus should equal(CastleStatus(true, true, false, true))
  }

  it should "be disabled for rook even if rook moves back to original position" in {
    val gameState = GameState("8/p7/8/8/8/8/8/4K2R w KQkq - 0 1")
    val updatedGameState1 = updateGameState(gameState, Position(7, 7), Position(6, 7))
    val updatedGameState2 = updateGameState(updatedGameState1, Position(1, 0), Position(2, 0))
    val updatedGameState3 = updateGameState(updatedGameState2, Position(6, 7), Position(7, 7))

    updatedGameState3.castleStatus should equal(CastleStatus(true, true, false, true))
  }

}
