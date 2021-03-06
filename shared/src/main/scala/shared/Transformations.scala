package shared

import shared.forms.InputTransformation
import shared.forms.InputTransformation.Message

object Transformations {
  def int: InputTransformation[String, Int] = InputTransformation(_.toInt)

  def long: InputTransformation[String, Long] = InputTransformation(_.toLong)

  def separatedValues(separator: String): InputTransformation[String, List[String]] = InputTransformation{str=>
    if (str == "") Nil
    else str.split(separator).toList
  }

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
