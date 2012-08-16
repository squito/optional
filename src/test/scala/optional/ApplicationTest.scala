package optional

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 *
 */

class ApplicationTest extends FunSuite with ShouldMatchers {

  test("parseArgs") {
    simpleArgs.main(Array("-a", "7", "-b", "18", "--longerName", "some string", "--lousyConflictingName", "another string"))
    simpleArgs.args._1 should be (7)
    simpleArgs.args._2 should be (18)
    simpleArgs.args._3 should be ("some string")
    simpleArgs.args._4 should be ("another string")

    simpleArgs.main(Array("--a", "9", "--b", "10", "--longerName", "abcde", "--lousyConflictingName", "efgh"))
    simpleArgs.args._1 should be (9)
    simpleArgs.args._2 should be (10)
    simpleArgs.args._3 should be ("abcde")
    simpleArgs.args._4 should be ("efgh")


    evaluating {
      simpleArgs.main(Array("-a", "90", "-b", "100", "-l", "xyz", "--lousyConflictingName", "pqr"))
    } should produce [Exception]
  }

}

object simpleArgs extends optional.Application {
  var args : (Int, Int, String, String) = _
  def main(a: Int, b : Int, longerName: String, lousyConflictingName: String) {
    args = (a, b, longerName, lousyConflictingName)
  }
}

