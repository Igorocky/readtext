package shared.forms

import shared.SharedConstants
import shared.Transformations._
import shared.Validations._
import shared.dto.{Paragraph, Topic}
import shared.forms.FormUtils._
import shared.pageparams.TextUI


object Forms {
  lazy val textForm = form[TextUI](
    SharedConstants.ID -> opt(onlyDigits >> long) ->
      ((_: TextUI).id.map(_.toString).getOrElse("")),
    SharedConstants.TITLE -> nonEmpty ->
      ((_: TextUI).title),
    SharedConstants.CONTENT -> nonEmpty ->
      ((_: TextUI).content)
  )

  lazy val paragraphForm = form[Paragraph](
    SharedConstants.ID -> opt(onlyDigits >> long) ->
      ((_: Paragraph).id.map(_.toString).getOrElse("")),
    SharedConstants.TITLE -> nonEmpty ->
      ((_: Paragraph).name)
  )

  lazy val topicForm = form[Topic](
    SharedConstants.ID -> opt(onlyDigits >> long) ->
      ((_: Topic).id.map(_.toString).getOrElse("")),
    SharedConstants.PARAGRAPH_ID -> opt(onlyDigits >> long) ->
      ((_: Topic).paragraphId.map(_.toString).getOrElse("")),
    SharedConstants.TITLE -> nonEmpty ->
      ((_: Topic).title),
    SharedConstants.IMAGES -> separatedValues(";") ->
      ((_: Topic).imagesStr)
  )

  lazy val tagForm = form[(Long, String)](
    SharedConstants.ID -> long -> ((t: (Long, String)) => t._1.toString),
    SharedConstants.TAG -> nonEmpty -> ((t: (Long, String)) => t._2)
  )
}
