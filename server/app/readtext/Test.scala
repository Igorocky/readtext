package readtext

import readtext.Combiners._
import readtext.Random._

object Test {
  def main(args: Array[String]): Unit = {
    def avg(l: List[Int]) = l.sum.toDouble / l.length
    def avgD(l: List[Double]) = l.sum / l.length
    val upperBound = 5
    val num = 200000
    val start = SimpleRng(System.currentTimeMillis())
    val avgTest: Rand[Double] = sequence(List.fill(num)(nonNegativeLessThen(upperBound))) map avg
    val avgTests: Rand[List[Double]] = sequence(List.fill(20)(avgTest))
    println(avgTests map avgD apply start)

  }
}
