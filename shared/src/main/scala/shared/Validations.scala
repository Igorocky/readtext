package shared

import shared.forms.InputValidation
import shared.messages.Messages

object Validations {
  def none[I]: InputValidation[I] = InputValidation(
    _ => true,
    _ => "This should never happen"
  )

  def maxLength(max: Int): InputValidation[String] = InputValidation(
    _.trim.length <= max,
    Messages.maxLength(max)(_)
  )

  def nonEmpty[T <% {def nonEmpty: Boolean}]: InputValidation[T] = InputValidation(
    _.nonEmpty,
    Messages.fieldShouldNotBeEmpty(_)
  )

  def onlyDigits: InputValidation[String] = InputValidation(
    _.forall(_.isDigit),
    Messages.fieldShouldContainOnlyDigits(_)
  )

  def range(min: Int, max: Int): InputValidation[Int] = InputValidation(
    i => min <= i && i <= max,
    _ => s"Value should be in range [$min, $max]"
  )
}
