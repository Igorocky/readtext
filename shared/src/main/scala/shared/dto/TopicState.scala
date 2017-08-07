package shared.dto

case class TopicState(topicId: Long,
                      easiness: Easiness,
                      score: Score,
                      time: Option[String],
                      duration: String
                     )