package models.rules

import models.rules.ValidMoves._
import test.framework.UnitSpec

class ValidMovesTest extends UnitSpec {

  /** En passant target present in pawn move if in correct position */

  /** En passant target not present in pawn move if not in correct position */

  /** Check disables other moves. */

  /** Cannot put self in check. */

  /** Cannot en passant if it puts self in check. */

  /** E2E valid moves count test (comparing to stockfish) */

}
