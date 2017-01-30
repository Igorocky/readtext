package shared.dto

case class Topic(
                  id: Option[Long] = None,
                  paragraphId: Option[Long] = None,
                  checked: Boolean = false,
                  title: String = "",
                  order: Int = 0,
                  images: List[String] = Nil
                )
