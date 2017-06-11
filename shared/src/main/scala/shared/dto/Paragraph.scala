package shared.dto

case class Paragraph(id: Option[Long] = None,
                     paragraphId: Option[Long] = None,
                     name: String,
                     expanded: Boolean = false,
                     order: Int = 0
                    ) {
  def update(upd: ParagraphUpdate) = copy(name = upd.name)
}

case class ParagraphUpdate(id: Long,
                           name: String
                          )
