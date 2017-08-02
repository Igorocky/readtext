package shared.dto

import java.time.ZonedDateTime

case class TextQA(id: Option[Long] = None,
                  cardId: Long,
                  text: String,
                  created: ZonedDateTime,
                  modified: ZonedDateTime
                 )

