package test

object TestUtils {

  /** Repeats a procedure n times */
  def repeat[A](n: Int)(procedure: => A): Unit = {
    if (n > 0) {
      procedure
      repeat(n - 1)(procedure)
    }
  }
}
