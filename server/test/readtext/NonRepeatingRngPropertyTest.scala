package readtext

import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}
import org.scalacheck.Arbitrary.arbitrary
import readtext.Combiners.sequence
import readtext.Random.nonRepeatingRng

object NonRepeatingRngPropertyTest extends Properties(NonRepeatingRng.getClass.getName) {

  def drainLengthValuesFromNewRng(length: Int): NonRepeatingRng => List[Int] =
    sequence(List.fill(length)(SFunc[NonRepeatingRng, Int](_.next))) map (_.distinct) onlyResult

    property("does not repeat generated numbers") = forAll(Gen.choose(0, 1000), arbitrary[Long]) { (length, seed) =>
      drainLengthValuesFromNewRng(length)(
        nonRepeatingRng(length, SimpleRng(seed))
      ).size == length
    }
}
