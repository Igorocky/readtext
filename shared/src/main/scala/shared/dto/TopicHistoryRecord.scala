package shared.dto

import java.time.ZonedDateTime

case class TopicHistoryRecord(topicId: Long,
                              score: Long,
                              activationTime: ZonedDateTime,
                              time: ZonedDateTime
                             )