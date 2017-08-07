package shared.dto

import java.time.ZonedDateTime

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

