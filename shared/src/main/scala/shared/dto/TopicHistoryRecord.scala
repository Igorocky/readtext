package shared.dto

import java.time.ZonedDateTime

case class TopicHistoryRecord(topicId: Long,
                              score: Long,
                              time: ZonedDateTime = ZonedDateTime.now()
                             )