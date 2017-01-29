package shared.dto

case class Paragraph(
                      id: Option[Long],
                      checked: Boolean,
                      name: String,
                      expanded: Boolean,
                      order: Int,
                      topics: List[Topic]
                    )
