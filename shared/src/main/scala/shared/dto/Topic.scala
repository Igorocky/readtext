package shared.dto

case class Topic(id: Option[Long] = None,
                 paragraphId: Long,
                 title: String = "",
                 order: Int = 0,
                 images: List[String] = Nil,
                 tags: List[String] = Nil
                )