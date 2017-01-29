package shared.dto

case class Topic(
                  id: Option[Int],
                  checked: Boolean,
                  title: String,
                  order: Int,
                  images: List[String]
                )
