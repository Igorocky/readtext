package readtext

import org.apache.commons.lang3.StringUtils
import readtext.ReadingPhase._
import readtext.TextFunctions._

import scala.collection.JavaConversions._

class ReadTextState(private val text: List[List[Word]], private val caretPosition: Int, val probabilityPercent: Int) {

    var currState: ReadingPhase = ONLY_TEXT

    var minMax: (Int, Int) = (0, 0)

    var currSentenceIdx = 0
    val sentenceCount: Int = text.length

    private var randomOrderOfSentences = false
    private val rndForSentenceIndex = new Rnd
    private var skipReadingStage = false

    var currSentence = Vector[Word]()

    private val rndIndices = new RandomIndices

    var selectedWord: Option[Word] = None

    init(caretPosition)

//    currSentenceIdx ==> ChgListener{chg=>
//        selectedWord.set(None)
//        resetCounters()
//    }
    def setCurrSentenceIdx(v: Int): Unit = {
        currSentenceIdx = v
        selectedWord = None
        resetCounters()
    }
//    currState ==> ChgListener{chg=>
//        selectedWord.set(None)
//    }
    def setCurrState(v: ReadingPhase): Unit = {
        currState = v
        selectedWord = None
    }

    def init(caretPosition: Int): Unit = {
        rndForSentenceIndex.refresh()
        if (caretPosition > 0) {
            selectWordByCaretPosition(caretPosition)
        }
        setCurrSentenceIdx(getSentenceWithCaretIdxOrZero(caretPosition))
        goToSentence(currSentenceIdx)
    }

    private def getSentenceWithCaretIdxOrZero(caretPosition: Int): Int = {
        var res = 0
        traverseAllWords{(s,l,r,w) =>
            if (l <= caretPosition && caretPosition <= r) {
                res = s
            }
        }
        res
    }

    private def traverseAllWords(consumer: (Int/*sentence index*/, Int/*leftPosition*/, Int/*rightPosition*/, Word) => Unit): Unit = {
        var leftPosition = 0
        var rightPosition = 0
        for (s <- 0 until text.length) {
            for (w <- 0 until text.get(s).length) {
                val word = text.get(s)(w)
                rightPosition += word.text.replaceAllLiterally("\r\n", "\n").length
                consumer(s, leftPosition, rightPosition, word)
                leftPosition = rightPosition
            }
        }
    }

    private def selectWordByCaretPosition(caretPosition: Int): Unit = {
        traverseAllWords{(s, l, r, w)=>
            if (w.hiddable && l <= caretPosition && caretPosition <= r) {
                traverseAllWords((s, l, r, w) => w.selected = false)
                selectWord(w)
            }
        }
    }

    def selectionRange: (Int, Int) = {
        var res = (0, 0)
        val selectedWord = getWordUnderFocus.orElse(this.selectedWord)
        if (selectedWord.isDefined) {
            traverseAllWords{(s,l,r,w) =>
                if (w == selectedWord.get) {
                    res = (l, r)
                }
            }
        } else if (currSentence.nonEmpty) {
            val sPos = getStartPositionOfWordInCurrSentence
            res = (sPos, sPos)
        }
        res
    }

    private def getStartPositionOfWordInCurrSentence: Int = {
        var res = 0
        if (currSentence.nonEmpty) {
            var firstNonemptyWordWasFound = false
            traverseAllWords{(s, l, r, w)=>
                if (currSentence.contains(w) && !firstNonemptyWordWasFound) {
                    res = l
                    if (StringUtils.replaceChars(w.text.trim, "\r\n", "").nonEmpty) {
                        firstNonemptyWordWasFound = true
                    }
                }
            }
        }
        res
    }

    private def parseText(text: String): List[List[Word]] = {
        splitTextOnSentences(text).map{ sentence =>
            splitSentenceOnParts(sentence).foldLeft((List[Word](), false)){
                case ((soFarRes, inComment), wordText) =>
                    val trimmedWordText = wordText.trim
                    val (isHiddable, inCommentNew) = if (trimmedWordText.contains("/*")) {
                        (false, true)
                    } else if (trimmedWordText.contains("*/")) {
                        (false, false)
                    } else {
                        (!inComment && TextFunctions.isHiddable(wordText), inComment)
                    }
                    (soFarRes:::(new Word(wordText, isHiddable))::Nil, inCommentNew)
            }._1
        }
    }

    private def resetWord(word: Word): Unit = {
        word.hidden = false
        word.awaitingUserInput = false
        word.userInputIsCorrect = None
        word.unsetUserInput()
        word.selected = false
    }

    def goToSentence(sentenceIdx: Int): Unit = {
        if (sentenceIdx >= 0 && sentenceIdx < text.length) {
            currSentence.clear()
            currSentence.addAll(text.get(sentenceIdx))
            setCurrSentenceIdx(sentenceIdx)
            currSentence.foreach(resetWord)
            setCurrState(ONLY_TEXT)
            if (skipReadingStage) {
                next()
            }
        }
    }

    def selectWord(word: Word): Unit = {
        currSentence.find(_.selected).foreach(_.selected = false)
        word.selected = true
        if (word.hidden && !word.awaitingUserInput) {
            focusWord(word)
        }
        selectedWord = Some(word)
    }

    def unselectWord(word: Word): Unit = {
        word.selected = false
        selectedWord = None
    }

    def getWordUnderFocus: Option[Word] = {
        currSentence.find(_.awaitingUserInput)
    }

    def setRandomOrderOfSentences(random: Boolean): Unit = {
        randomOrderOfSentences = random
    }

    def setSkipReadingStage(skipReadingStage: Boolean): Unit = {
        this.skipReadingStage = skipReadingStage
    }

    def next(): Unit = {
        if (currState == ONLY_TEXT) {
            hideWordsOfCurrentSentence()
            setCurrState(TEXT_WITH_INPUTS)
        } else if (currState == TEXT_WITH_INPUTS) {
            nextSentence()
        }
    }

    private def hideWordsOfCurrentSentence(): Unit = {
        currSentence.foreach(resetWord)
        val hidableWords = currSentence.filter(_.hiddable).toList
        rndIndices.getRandomIndices(
            hidableWords.length,
            probabilityPercent,
            text(currSentenceIdx).hashCode()
        ).foreach(hidableWords(_).hidden = true)
        minMax = rndIndices.getMinMax
        currSentence.find(_.hidden).foreach(_.awaitingUserInput = true)
    }

    def refreshHiddenWords(): Unit = {
        if (currState == TEXT_WITH_INPUTS) {
            hideWordsOfCurrentSentence()
        }
    }

    def nextSentence(): Unit = {
        if (randomOrderOfSentences) {
            setCurrSentenceIdx(rndForSentenceIndex.nextInt(text.size))
            goToSentence(currSentenceIdx)
        } else {
            if (currSentenceIdx < text.size - 1) {
                setCurrSentenceIdx(currSentenceIdx + 1)
                goToSentence(currSentenceIdx)
            } else {
//                gotoNotLoadedState()
                ???
            }
        }
    }

    def back(): Unit = {
        if (currState == TEXT_WITH_INPUTS) {
            goToSentence(currSentenceIdx)
        } else if (currState == ONLY_TEXT) {
            if (currSentenceIdx > 0) {
                setCurrSentenceIdx(currSentenceIdx - 1)
                goToSentence(currSentenceIdx)
            } else {
//                gotoNotLoadedState()
                ???
            }
        }
    }

    def gotoNextWordToBeEnteredOrSwitchToNextSentence(autoRepeat: Int): Unit = {
        val curWord = currSentence.find(_.awaitingUserInput)
        currSentence.foreach(_.awaitingUserInput = false)
        val hiddenWords = currSentence.filter(_.hidden)
        val firstWordWithoutUserInput = curWord
            .flatMap(cw => hiddenWords.dropWhile(_ != curWord.get).find(_.getUserInput.isEmpty))
            .orElse(hiddenWords.find(_.getUserInput.isEmpty))
        if (firstWordWithoutUserInput.isDefined) {
            firstWordWithoutUserInput.get.awaitingUserInput = true
        } else {
            val thereWereUncheckedWordsExceptCurrent = hiddenWords
                .filter(_ != curWord.getOrElse(null))
                .find(_.userInputIsCorrect.isEmpty).isDefined
            val thereWereUncheckedWords = hiddenWords
                .find(_.userInputIsCorrect.isEmpty).isDefined
            hiddenWords.filter(_.userInputIsCorrect.isEmpty).foreach(w=>
                w.userInputIsCorrect = Some(TextFunctions.checkUserInput(w.text, w.getUserInput.get))
            )
            val firstIncorrectWord = (if (thereWereUncheckedWordsExceptCurrent) None else curWord)
                .flatMap(cw => hiddenWords.dropWhile(_ != curWord.get).find(!_.userInputIsCorrect.get))
                .orElse(hiddenWords.find(!_.userInputIsCorrect.get))
            if (firstIncorrectWord.isDefined) {
                firstIncorrectWord.get.awaitingUserInput = true
            } else if (!thereWereUncheckedWords) {
                if (minMax._1 >= autoRepeat) {
                    next()
                } else {
                    refreshHiddenWords()
                }
            }
        }
    }

    def selectNextWord(step: Int): Unit = {
        val selectedWordOpt = selectedWord
        var idx = if (selectedWordOpt.isDefined) {
            unselectWord(selectedWordOpt.get)
            currSentence.indexOf(selectedWordOpt.get)
        } else {
            -1
        }

        def incSelectedWordIdx() = {
            idx += step
            if (idx > currSentence.length - 1) {
                idx = -1
            } else if (idx < -1) {
                idx = currSentence.length - 1
            }
        }
        def isIdxAppropriate() = {
            idx == -1 || currSentence.get(idx).hiddable
        }

        incSelectedWordIdx()
        while(!isIdxAppropriate()) {
            incSelectedWordIdx()
        }
        if (idx != -1) {
            selectWord(currSentence.get(idx))
        }
    }

    def focusWord(word: Word): Unit = {
        currSentence.foreach(_.awaitingUserInput = false)
        word.awaitingUserInput = true
        selectWord(word)
    }

    def resetCounters(): Unit = {
        rndIndices.resetCounters()
        minMax = (0, 0)
    }
}
