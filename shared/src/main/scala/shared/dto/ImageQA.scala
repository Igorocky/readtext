package shared.dto

import java.time.ZonedDateTime

case class ImageQA(id: Option[Long] = None,
                   cardId: Long,
                   imagePaths: List[String],
                   created: ZonedDateTime,
                   modified: ZonedDateTime
                       )

