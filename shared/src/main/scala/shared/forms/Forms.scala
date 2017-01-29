package shared.forms

import shared.SharedConstants
import shared.Transformations._
import shared.Validations._
import shared.dto.Paragraph
import shared.forms.FormUtils._
import shared.pageparams.TextUI


object Forms {
  lazy val textFrom = form[TextUI](
    SharedConstants.ID -> opt(onlyDigits >> long) ->
      ((_: TextUI).id.map(_.toString).getOrElse("")),
    SharedConstants.TITLE -> nonEmpty ->
      ((_: TextUI).title),
    SharedConstants.CONTENT -> nonEmpty ->
      ((_: TextUI).content)
  )

  lazy val paragraphFrom = form[Paragraph](
    SharedConstants.ID -> opt(onlyDigits >> long) ->
      ((_: Paragraph).id.map(_.toString).getOrElse("")),
    SharedConstants.TITLE -> nonEmpty ->
      ((_: Paragraph).name)
  )
}
