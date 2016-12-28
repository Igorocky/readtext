package readtext

import java.util.Random

class RandomIndices {
    import RandomIndices._

    private var lastWordsCounts = List[Int]()
    private var lastHash = 0L

    def getRandomIndices(elemsCnt: Int, pct: Int, hash: Long): List[Int] = {
        if (elemsCnt != lastWordsCounts.length || hash != lastHash) {
            lastWordsCounts = List.fill(elemsCnt)(0)
            lastHash = hash
        }
        val res = (if (pct <= 50) {
            getRandomIndicesUnder50(elemsCnt, pct, lastWordsCounts)
        } else if (pct < 100) {
            (0 until elemsCnt).toList diff getRandomIndicesUnder50(elemsCnt, 100 - pct, lastWordsCounts.map(-_))
        } else {
            (0 until elemsCnt).toList
        })

        lastWordsCounts = lastWordsCounts.zipWithIndex.map{case (c,i) => if (res.contains(i)) c + 1 else c}
        res
    }

    def getLastWordsCounts: List[Int] = lastWordsCounts

    def getMinMax = (lastWordsCounts.min, lastWordsCounts.max)

    def resetCounters() {
        lastWordsCounts = List(0)
    }
}

object RandomIndices {
    private val rnd = new Random()

    protected[readtext] def calcShift(baseIdx: Int, lastWordsCounts: List[Int]): Int = {
        val leftIdx = if (baseIdx > 0) baseIdx - 1 else lastWordsCounts.length - 1
        val rightIdx = if (baseIdx < lastWordsCounts.length - 1) baseIdx + 1 else 0
        findIdxWithMinCnt(
            List(
                lastWordsCounts(leftIdx),
                lastWordsCounts(baseIdx),
                lastWordsCounts(rightIdx)
            ),
        Nil
        ) - 1
    }

    protected[readtext] def findIdxWithMinCnt(counts: List[Int], alreadySelectedIndices: List[Int]): Int = {
        val countsWithIndices = counts.zipWithIndex.filter{case (c,i) => !alreadySelectedIndices.contains(i)}
        val minCnt = countsWithIndices.map(_._1).min
        val indicesWithMinCount = countsWithIndices.filter{case (c,i) => c == minCnt}.map(_._2)
        indicesWithMinCount(rnd.nextInt(indicesWithMinCount.length))
    }

    protected[readtext] def getRandomIndicesUnder50(elemsCnt: Int, pct: Int, lastWordsCounts: List[Int]): List[Int] = {
        val resLength = math.round(elemsCnt * pct / 100.0).toInt max 1
        val step = math.round(elemsCnt.toDouble / resLength).toInt max 1
        val res = (2 to resLength).foldLeft(List(findIdxWithMinCnt(lastWordsCounts, Nil))) {(soFarRes, i) =>
            val baseIdx = (soFarRes.head + step) % elemsCnt

            def findNextIdx(baseIdx: Int): Int = {
                val res = (elemsCnt + baseIdx + calcShift(baseIdx, lastWordsCounts)) % elemsCnt
                if (!soFarRes.contains(res)) {
                    res
                } else {
                    findIdxWithMinCnt(lastWordsCounts, soFarRes)
                }
            }

            val nextIdx = findNextIdx(baseIdx)
            nextIdx::soFarRes
        }
        res
    }
}