package app.components.forms

import app.Utils._

object SubmitButton {
  def apply(name: String)(implicit formParams: FormCommonParams) = buttonWithText(
    onClick = formParams.submit,
    btnType = if (formParams.formData.hasErrors) BTN_DANGER else BTN_PRIMARY,
    text = name,
    disabled = formParams.formData.hasErrors
  )
}