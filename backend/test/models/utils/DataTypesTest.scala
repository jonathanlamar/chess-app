package models.utils

import models.utils.Fen.{parseFenString, toFenString}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers._
import test.TestData._
import test.TestUtils._

abstract class UnitSpec extends AnyFlatSpec with should.Matchers

class DataTypesTest extends UnitSpec {

  "toFenString(parseFenString)" should "be the identity" in {
    repeat(10) {
      val fenString = getFenString()

      toFenString(parseFenString(fenString)) should equal(fenString)
    }
  }

  "parseFenString(toFenString)" should "be the identity" in {
    repeat(10) {
      val gameState = getGameState()

      parseFenString(toFenString(gameState)) should equal(gameState)
    }
  }
}
