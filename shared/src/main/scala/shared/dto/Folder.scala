package shared.dto

import java.time.ZonedDateTime

case class Folder(id: Option[Long] = None,
                  parentFolderId: Option[Long] = None,
                  name: String,
                  order: Int,
                  created: ZonedDateTime
                 )