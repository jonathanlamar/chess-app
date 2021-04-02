package models.utils

import models.utils.DataTypes._
import models.utils.Fen.{parseFenString, toFenString}
import test.framework.UnitSpec

class DataTypesTest extends UnitSpec {

  behavior of "FEN encoding and decoding"

  it should "be left inverse of each other" in {
    repeat(10) {
      val fenString = getFenString()

      toFenString(parseFenString(fenString)) should equal(fenString)
    }
  }

  it should "be right inverse of each other" in {
    repeat(10) {
      val gameState = getGameState()

      parseFenString(toFenString(gameState)) should equal(gameState)
    }
  }
}
