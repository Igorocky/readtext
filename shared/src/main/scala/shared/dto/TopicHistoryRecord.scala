package shared.dto

import java.time.ZonedDateTime

import shared.SharedConstants._
import shared.dto.EasinessLevels.{EASY, HARD, MEDIUM}
import shared.dto.ScoreLevels.{BAD, EXCELLENT, GOOD, POOR}
import shared.utils.Enum

case class TopicHistoryRecord(topicId: Long,
                              easiness: Easiness,
                              score: Score,
                              time: ZonedDateTime = ZonedDateTime.now()
                             )

case class Easiness(level: Int)

object EasinessLevels extends Enum[Easiness] {
  private def level(lev: Int) = addElem(Easiness(lev))

  val EASY = level(2)
  val MEDIUM = level(1)
  val HARD = level(0)
  end
}

case class Score(level: Int)

object ScoreLevels extends Enum[Score] {
  private def level(lev: Int) = addElem(Score(lev))

  val BAD = level(0)
  val POOR = level(1)
  val GOOD = level(2)
  val EXCELLENT = level(3)
  end
}

object TopicHistoryRecordUtils {
  def easinessClass(easiness: Easiness) = easiness match {
    case EASY => EASINESS_EASY
    case MEDIUM => EASINESS_MEDIUM
    case HARD => EASINESS_HARD
  }

  def easinessStr(easiness: Easiness) = easiness match {
    case EASY => "Easy"
    case MEDIUM => "Medium"
    case HARD => "Hard"
  }

  def scoreClass(score: Score) = score match {
    case EXCELLENT => SCORE_EXCELLENT
    case GOOD => SCORE_GOOD
    case POOR => SCORE_POOR
    case BAD => SCORE_BAD
  }

  def scoreStr(score: Score) = score match {
    case EXCELLENT => "Excellent"
    case GOOD => "Good"
    case POOR => "Poor"
    case BAD => "Bad"
  }
}