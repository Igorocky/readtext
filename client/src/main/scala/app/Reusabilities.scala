package app

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.Reusability
import shared.dto.{Paragraph, Topic}
import shared.messages.Language

object Reusabilities {
  implicit val paragraphReuse = Reusability.byRef[Paragraph]
  implicit val languageReuse = Reusability.byRef[Language]
  implicit val topicReuse = Reusability.byRef[Topic]
  implicit val callbackReuse = Reusability[Callback](_ == _)
}
