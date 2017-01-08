package readtext

import org.scalatest.{FunSuite, Matchers}
import readtext.Combiners._
import readtext.Random._

class NonRepeatingRngTest extends FunSuite with Matchers {
  test("NonRepeatingRng should not repeat generated numbers") {
    val numberOfTests = 300
    val length = 100
    val isOk: NonRepeatingRng => Boolean =
      sequence(List.fill(length)(Gen[NonRepeatingRng, Int](_.next))) map (_.distinct.size == length) onlyResult
    val seedGen: Rand[Int] = nonNegativeLessThen(1000000)
    val allTests: RNG => Boolean = sequence(List.fill(numberOfTests)(seedGen))
      .map(_ map(i => nonRepeatingRng(length, SimpleRng(i))))
      .map(_ forall isOk)
      .onlyResult

    allTests(SimpleRng(System.currentTimeMillis())) should be(true)
  }

  test("NonRepeatingRng should correctly remove specified indices") {
  }
}
