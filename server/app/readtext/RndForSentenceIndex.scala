package readtext

import java.util.Random

import scala.collection.mutable.ListBuffer

class RndForSentenceIndex {
    private val rnd = new Random()
    private val buf = ListBuffer[Int]()
    private var lastBound = -1

    def nextInt(bound: Int) = {
        if (buf.isEmpty || lastBound != bound) {
            lastBound = bound
            buf.clear()
            buf ++= 0 until bound
            for (i <- 1 to bound*5) {
                buf += buf.remove(rnd.nextInt(buf.size))
            }
        }
        val res = buf.remove(rnd.nextInt(buf.size)) % bound
        res
    }

    def refresh() = lastBound = -1
}