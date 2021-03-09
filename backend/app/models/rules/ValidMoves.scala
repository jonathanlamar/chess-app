package models.rules

import models.utils.DataTypes._

object ValidMoves {

  // TODO: For testing API before logic is written
  def allPossibleMovesDumb(pos: Position): List[Position] = {
    val positions =
      for (
        r <- 0 until 8 if r != pos.row;
        c <- 0 until 8 if c != pos.col
      ) yield Position(r, c)

    // Not sure when to use list, array, sequence
    positions.toList
  }
}
