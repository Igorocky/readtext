package shared

import shared.forms.FormUtils.Message
import shared.forms.InputTransformation

object Transformations {
  def int: InputTransformation[String, Int] = InputTransformation(_.toInt)
  def long: InputTransformation[String, Long] = InputTransformation(_.toLong)

  def opt[T](other: InputTransformation[String, T]): InputTransformation[String, Option[T]] = new InputTransformation[String, Option[T]] {
    def apply(str: String): Either[List[Message], Option[T]] = {
      if (str.trim.isEmpty) {
        Right(None)
      } else {
        other(str).right.map(Some(_))
      }
    }
  }
}
