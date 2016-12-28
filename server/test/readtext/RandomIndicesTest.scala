package readtext

import org.junit.Assert._
import org.scalatest.{FlatSpec, Matchers}

import scala.annotation.tailrec

class RandomIndicesTest extends FlatSpec with Matchers {
  "RandomIndices" should "testGetRandomIndicesLength" in {
    val rnd = new RandomIndices
    assertEquals(3, rnd.getRandomIndices(9, 30, 1).size)
    assertEquals(2, rnd.getRandomIndices(8, 30, 1).size)
    assertEquals(1, rnd.getRandomIndices(15, 0, 1).size)
    assertEquals(73, rnd.getRandomIndices(73, 100, 1).size)
  }

  "RandomIndices" should "testGetRandomIndicesStep" in {
    val elemsCnt = 1000

    @tailrec
    def findMinMaxDif(nums: List[Int], min: Int = Int.MaxValue, max: Int = Int.MinValue): (Int, Int) = {
      val diffMayBeNegative = nums.tail.head - nums.head
      val diff = if (diffMayBeNegative >= 0) diffMayBeNegative else elemsCnt + diffMayBeNegative
      val newMin = if (diff < min) diff else min
      val newMax = if (diff > max) diff else max
      if (nums.size == 2) {
        (newMin, newMax)
      } else {
        findMinMaxDif(nums.tail, newMin, newMax)
      }
    }

    for (i <- 1 to 100) {
      val inds = new RandomIndices().getRandomIndices(elemsCnt, 15, 1).reverse
      assertEquals(150, inds.length)
      val (min, max) = findMinMaxDif(inds.take(inds.length - 15))
      assertEquals(6, min)
      assertEquals(8, max)
    }
  }

  "RandomIndices" should "testGetRandomIndicesUniqueness" in {
    val rnd = new RandomIndices
    for (i <- 1 to 100) {
      val res = rnd.getRandomIndices(10, 35, 1)
      assertEquals(4, res.length)
      assertEquals(res.length, res.toSet.size)
    }
  }

  "RandomIndices" should "testSecondVersionOfCalcShiftNormalCase" in {
    val baseIdx = 1
    assertEquals(-1, RandomIndices.calcShift(baseIdx, List(1, 2, 3)))
    assertEquals(0, RandomIndices.calcShift(baseIdx, List(2, 1, 3)))
    assertEquals(1, RandomIndices.calcShift(baseIdx, List(2, 3, 1)))

    for (i <- 1 to 20) {
      val shift = RandomIndices.calcShift(baseIdx, List(2, 1, 1))
      assertTrue(shift == 0 || shift == 1)
    }
  }

  "RandomIndices" should "testSecondVersionOfCalcShiftLeftBoundaryCase" in {
    val buf = List(4, 10, 0)
    val baseIdx = 0
    assertEquals(-1, RandomIndices.calcShift(baseIdx, buf))
  }

  "RandomIndices" should "testSecondVersionOfCalcShiftRightBoundaryCase" in {
    val buf = List(0, 10, 4)
    val baseIdx = 2
    assertEquals(1, RandomIndices.calcShift(baseIdx, buf))
  }

  "RandomIndices" should "testLastWordsCounts" in {
    testLastWordsCounts(20, 30, 6)
    testLastWordsCounts(20, 70, 6)
  }

  def testLastWordsCounts(elemsCnt: Int, pct: Int, iterNum: Int): Unit = {
    val rnd = new RandomIndices

    @tailrec
    def findMinMax(nums: List[Int], min: Int = Int.MaxValue, max: Int = Int.MinValue): (Int, Int) = {
      val num = nums.head
      val newMin = if (num < min) num else min
      val newMax = if (num > max) num else max
      if (nums.size == 2) {
        (newMin, newMax)
      } else {
        findMinMax(nums.tail, newMin, newMax)
      }
    }

    @tailrec
    def checkBuf(level: Int, prevBuf: List[Int] = Nil, acumRes: List[Int] = Nil): List[Int] = {
      if (level <= 0) {
        acumRes
      } else {
        val idxs = rnd.getRandomIndices(elemsCnt, pct, 1)
        val buf = rnd.getLastWordsCounts
        //                println("------------------------------------------------------------------")
        //                println(s"idxs = $idxs")
        //                println(s"buf = $buf")
        val (min, max) = findMinMax(buf)
        val dif = max - min
        //                println(s"MinMax = ${(min, max)}, dif = $dif (${dif/max.toDouble*100}%)")
        assertEquals(elemsCnt, buf.length)
        assertEquals(min, rnd.getMinMax._1)
        assertEquals(max, rnd.getMinMax._2)
        if (prevBuf.isEmpty) {
          assertTrue(buf.zipWithIndex.forall { case (c, i) =>
            if (idxs.contains(i)) c == 1 else c == 0
          })
        } else {
          assertTrue(buf.zipWithIndex.forall { case (c, i) =>
            if (idxs.contains(i)) c == prevBuf(i) + 1 else c == prevBuf(i)
          })
        }
        checkBuf(level - 1, buf, dif :: acumRes)
      }
    }

    val diffs = checkBuf(iterNum)
    assertEquals(iterNum, diffs.length)
    assertTrue(diffs.max <= 2)
  }

  "RandomIndices" should "testFindIdxWithMinCnt_emptyList" in {
    val buf = List(1, 2, 1, 2, 3, 1, 4, 3, 3)
    for (i <- 1 to 20) {
      val minCntIdx = RandomIndices.findIdxWithMinCnt(buf, Nil)
      assertTrue(minCntIdx == 0 || minCntIdx == 2 || minCntIdx == 5)
    }
  }

  "RandomIndices" should "testFindIdxWithMinCnt_nonemptyList" in {
    val buf = List(1, 2, 1, 2, 3, 1, 4, 3, 3)
    for (i <- 1 to 20) {
      val minCntIdx = RandomIndices.findIdxWithMinCnt(buf, List(0, 2))
      assertEquals(5, minCntIdx)
    }
  }
}
