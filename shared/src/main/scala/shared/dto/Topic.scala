package shared.dto

case class Topic(id: Option[Long] = None,
                 paragraphId: Option[Long] = None,
                 title: String = "",
                 order: Int = 0,
                 images: List[String] = Nil,
                 tags: List[String] = Nil
                ) {
  def update(upd: TopicUpdate) = copy(title = upd.title, images = upd.images)
}

case class TopicUpdate(id: Long,
                       title: String = "",
                       images: List[String] = Nil)
