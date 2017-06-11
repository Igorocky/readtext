package shared.forms

import shared.StrUtils
import shared.Transformations._
import shared.Validations._
import shared.dto.{Paragraph, Topic}
import shared.forms.FormUtils._


object Forms {
  lazy val paragraphForm = form[Paragraph](
    FormKeys.ID -> opt(onlyDigits >> long) ->
      ((_: Paragraph).id.map(_.toString).getOrElse("")),
    FormKeys.TITLE -> nonEmpty ->
      ((_: Paragraph).name)
  )

  lazy val topicForm = form[Topic](
    FormKeys.ID -> opt(onlyDigits >> long) ->
      ((_: Topic).id.map(_.toString).getOrElse("")),
    FormKeys.PARAGRAPH_ID -> opt(onlyDigits >> long) ->
      ((_: Topic).paragraphId.map(_.toString).getOrElse("")),
    FormKeys.TITLE -> nonEmpty ->
      ((_: Topic).title),
    FormKeys.IMAGES -> separatedValues(";") ->
      ((t: Topic) => StrUtils.listToStr(t.images))
  )

  lazy val tagForm = form[(Long, String)](
    FormKeys.PARENT_ID -> long -> ((t: (Long, String)) => t._1.toString),
    FormKeys.TAG -> nonEmpty -> ((t: (Long, String)) => t._2)
  )
}
