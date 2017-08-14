package shared.dto

import java.time.ZonedDateTime

case class TopicHistoryRecord(topicId: Long,
                              score: Long,
                              comment: String,
                              activationTime: ZonedDateTime,
                              time: ZonedDateTime
                             )