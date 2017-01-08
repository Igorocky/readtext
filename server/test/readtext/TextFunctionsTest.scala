package readtext

import org.junit.{Assert}
import org.scalatest.{FlatSpec, Matchers}

class TextFunctionsTest extends FlatSpec with Matchers {
    "TextFunctions" should "canWordBeHiddenTest" in {
        Assert.assertTrue(TextFunctions.isHiddable("abc"))
        Assert.assertFalse(TextFunctions.isHiddable(","))
        Assert.assertFalse(TextFunctions.isHiddable(", "))
        Assert.assertFalse(TextFunctions.isHiddable("– "))
        Assert.assertFalse(TextFunctions.isHiddable("\""))
        Assert.assertFalse(TextFunctions.isHiddable(": ["))
        Assert.assertFalse(TextFunctions.isHiddable("] ( "))
        Assert.assertFalse(TextFunctions.isHiddable(")) "))
        Assert.assertFalse(TextFunctions.isHiddable(" / "))
        Assert.assertFalse(TextFunctions.isHiddable(" \\"))
        Assert.assertFalse(TextFunctions.isHiddable("; "))
        Assert.assertFalse(TextFunctions.isHiddable("\u2014"))
        Assert.assertFalse(TextFunctions.isHiddable(" \u2014 "))
        Assert.assertFalse(TextFunctions.isHiddable("!"))
        Assert.assertFalse(TextFunctions.isHiddable("? "))
        Assert.assertFalse(TextFunctions.isHiddable("\u2026 "))
        Assert.assertFalse(TextFunctions.isHiddable("\u201E "))
        Assert.assertFalse(TextFunctions.isHiddable("\u201D "))
    }

    "TextFunctions" should "splitTextOnSentencesTest1" in {
        val text = "Word1 word2. Word3 word4 word5. Word6.\r\nWord7.\nWord8\r.Word9!Word10?Word12...Word11"

        val sentences = TextFunctions.splitTextOnSentences(text)
        Assert.assertEquals("Word1 word2.", sentences(0))
        Assert.assertEquals(" Word3 word4 word5.", sentences(1))
        Assert.assertEquals(" Word6.", sentences(2))
        Assert.assertEquals("\r\nWord7.", sentences(3))
        Assert.assertEquals("\nWord8\r.", sentences(4))
        Assert.assertEquals("Word9!", sentences(5))
        Assert.assertEquals("Word10?", sentences(6))
        Assert.assertEquals("Word12...", sentences(7))
        Assert.assertEquals("Word11", sentences(8))
    }

    "TextFunctions" should "splitTextOnSentencesTest2" in {
        val text = "Word1\u2026 Word2"

        val sentences = TextFunctions.splitTextOnSentences(text)
        Assert.assertEquals("Word1\u2026", sentences(0))
        Assert.assertEquals(" Word2", sentences(1))
    }

    "TextFunctions" should "splitSentenceOnPartsTest1" in {
        val sentence = "Word1 word2, \"word3\" - word4-suff (word5, word6!): word7; word8. word9?" +
          " WordWith'Apostrophe. WordWith–LongHyphen. word10... "

        val parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertTrue(parts.contains("Word1"))
        Assert.assertTrue(parts.contains("word2"))
        Assert.assertTrue(parts.contains("word3"))
        Assert.assertTrue(parts.contains("word4-suff"))
        Assert.assertTrue(parts.contains("word5"))
        Assert.assertTrue(parts.contains("word6"))
        Assert.assertTrue(parts.contains("word7"))
        Assert.assertTrue(parts.contains("word8"))
        Assert.assertTrue(parts.contains("WordWith'Apostrophe"))
        Assert.assertTrue(parts.contains("WordWith–LongHyphen"))
        Assert.assertTrue(parts.contains("word9"))
        Assert.assertTrue(parts.contains("? "))
        Assert.assertTrue(parts.contains("word10"))
        Assert.assertTrue(parts.contains("... "))
    }

    "TextFunctions" should "splitSentenceOnPartsTest2" in {
        var sentence = "\r\nWo\r\nrd1\r\n"

        var parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\r\n", parts(0))
        Assert.assertEquals("Wo", parts(1))
        Assert.assertEquals("\r\n", parts(2))
        Assert.assertEquals("rd1", parts(3))
        Assert.assertEquals("\r\n", parts(4))

        sentence = "\nWo\nrd1\n"

        parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\n", parts(0))
        Assert.assertEquals("Wo", parts(1))
        Assert.assertEquals("\n", parts(2))
        Assert.assertEquals("rd1", parts(3))
        Assert.assertEquals("\n", parts(4))

        sentence = "\n\nWo\r\n\r\nrd1\r\n"

        parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\n\n", parts(0))
        Assert.assertEquals("\r\n\r\n", parts(2))
    }

    "TextFunctions" should "splitSentenceOnPartsTest3" in {
        val parts = TextFunctions.splitSentenceOnParts("\n\n\u2014 ")
        Assert.assertEquals("\n\n", parts(0))
        Assert.assertEquals("\u2014", parts(1))
        Assert.assertEquals(" ", parts(2))
    }

    "TextFunctions" should "splitSentenceOnPartsTest4" in {
        val parts = TextFunctions.splitSentenceOnParts("\u201EWord1\u201D")
        Assert.assertEquals("\u201E", parts(0))
        Assert.assertEquals("Word1", parts(1))
        Assert.assertEquals("\u201D", parts(2))
    }

    "TextFunctions" should "splitSentenceOnPartsTest5" in {
        val parts = TextFunctions.splitSentenceOnParts("/*Word1*/")
        Assert.assertEquals("/*", parts(0))
        Assert.assertEquals("Word1", parts(1))
        Assert.assertEquals("*/", parts(2))
    }

    "TextFunctions" should "checkUserInputTest" in {
        Assert.assertTrue(TextFunctions.checkUserInput("abc", "abc"))
        Assert.assertFalse(TextFunctions.checkUserInput("abc", "mnk"))
    }
}
