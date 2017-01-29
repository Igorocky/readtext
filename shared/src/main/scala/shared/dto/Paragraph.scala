package shared.dto

case class Paragraph(
                      id: Option[Long] = None,
                      checked: Boolean = false,
                      name: String,
                      expanded: Boolean = false,
                      order: Int = 0,
                      topics: List[Topic] = Nil
                    )
