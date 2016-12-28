package readtext

import org.junit.Assert._
import org.scalatest.{FlatSpec, Matchers}

class RndTest extends FlatSpec with Matchers {
    "Rnd" should "testNextInt" in {
        val rnd = new Rnd
        val boundary1 = 100
        for (i <- 1 to 100) {
            val set1 = (for (n <- 1 to boundary1) yield {
                rnd.nextInt(boundary1)
            }).toSet
            assertEquals(boundary1, set1.size)
        }
        rnd.nextInt(boundary1)
        rnd.nextInt(boundary1)

        //Rnd should create new buffer if boundary changes
        val boundary2 = 10
        val set2 = (for (n <- 1 to boundary2) yield {
            rnd.nextInt(boundary2)
        }).toSet
        assertEquals(boundary2, set2.size)
        rnd.nextInt(boundary2)
        rnd.nextInt(boundary2)

        val boundary3 = 2
        val set3 = (for (n <- 1 to boundary3) yield {
            rnd.nextInt(boundary3)
        }).toSet
        assertEquals(boundary3, set3.size)
    }

    "Rnd" should "testRemoveFromBuffer" in {
        val rnd = new Rnd
        val boundary = 10
        val firstVal = rnd.nextInt(boundary)
        rnd.removeFromBuffer(List(1,2,3,4,5))
        val buf = rnd.getBuffer
        assertFalse(buf.contains(firstVal))
        assertFalse(buf.contains(1))
        assertFalse(buf.contains(2))
        assertFalse(buf.contains(3))
        assertFalse(buf.contains(4))
        assertFalse(buf.contains(5))
    }
}
