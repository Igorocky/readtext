package shared.dto

case class Paragraph(id: Option[Long] = None,
                     paragraphId: Option[Long] = None,
                     name: String,
                     expanded: Boolean = false,
                     order: Int = 0
                    )