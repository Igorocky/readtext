package readtext

import readtext.Combiners._
import readtext.Random._

import scala.collection.immutable.Stream.{continually, iterate}

trait RNG {
  def next: (Int, RNG)
}

case class SimpleRng(seed: Long) extends RNG {
  override def next: (Int, RNG) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val res = (newSeed >>> 16).toInt
    (res, SimpleRng(newSeed))
  }
}

case class NonRepeatingRng(length: Int, buf: List[Int], rng: RNG) {
  def next: (Int, NonRepeatingRng) =
    if (buf.nonEmpty) {
      (buf.head, copy(buf = buf.tail))
    } else {
      nonRepeatingRng(length, rng).next
    }

  def removeFromBuffer(nums: List[Int]): NonRepeatingRng = copy(buf = buf diff nums)
}

object Random {
  type Rand[T] = Gen[RNG, T]

  def rng: Rand[Int] = Gen(_.next)
  def nonNegativeLessThen(upperBound: Int): Rand[Int] = rng map (_.abs % upperBound)

  def nonRepeatingRng(length: Int, rng: RNG): NonRepeatingRng = {
    val (newBuf, newRng) = shuffle((0 until length).toList)(rng)
    NonRepeatingRng(length, newBuf, newRng)
  }

  def shuffle(list: List[Int]): Gen[RNG, List[Int]] =
    sequence(
      iterate(nonNegativeLessThen(list.length))(g => g).take(list.length*5),
      list
    )(swap)

  /**
    * 0 <= n <= v.length
    * @param n
    * @param list
    */
  def swap(list: List[Int], n: Int) = {
    val (l,r) = list.splitAt(n)
    r ::: l
  }
}
