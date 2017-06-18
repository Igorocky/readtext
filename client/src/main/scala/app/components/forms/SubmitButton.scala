package app.components.forms

import app.Utils._

object SubmitButton {
  def apply[T, S](name: String)(implicit formParams: FormCommonParams[T, S]) = buttonWithText(
    onClick = formParams.submit,
    btnType = if (formParams.formData.hasErrors) BTN_DANGER else BTN_PRIMARY,
    text = name,
    disabled = formParams.formData.hasErrors
  )
}