package shared.dto

trait TopicLike {
  val images: List[String]
  def setImages(imgs: List[String]): this.type

  def imagesStr = images.mkString(";")

  def setImages(imgsStr: String): this.type = setImages(
    if (imgsStr == null || imgsStr.trim == "") Nil else imgsStr.split(";").toList
  )
}

case class Topic(id: Option[Long] = None,
                 paragraphId: Option[Long] = None,
                 checked: Boolean = false,
                 title: String = "",
                 order: Int = 0,
                 images: List[String] = Nil,
                 tags: List[String] = Nil
                ) extends TopicLike {
  override def setImages(imgs: List[String]): Topic.this.type = copy(images = imgs).asInstanceOf[Topic.this.type]

  def setTags(tags: List[String]): Topic.this.type = copy(tags = tags).asInstanceOf[Topic.this.type]
  def setTags(tagsStr: String): Topic.this.type = setTags(
    if (tagsStr == null || tagsStr.trim == "") Nil else tagsStr.split(";").toList
  )
  def tagsStr = tags.mkString(";")
}

case class TopicUpdate(id: Long,
                       title: String = "",
                       images: List[String] = Nil) extends TopicLike {
  override def setImages(imgs: List[String]): TopicUpdate.this.type = copy(images = imgs).asInstanceOf[TopicUpdate.this.type]
}
