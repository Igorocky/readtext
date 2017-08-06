package shared.dto

import java.time.ZonedDateTime

case class Folder(id: Option[Long] = None,
                  sourceId: String = "",
                  parentFolderId: Option[Long] = None,
                  name: String = "",
                  order: Int = 0,
                  created: ZonedDateTime = ZonedDateTime.now(),
                  modified: ZonedDateTime = ZonedDateTime.now()
                 )