package shared.dto

case class TopicState(topicId: Long,
                      score: Long,
                      time: Option[String],
                      duration: String
                     )