package app.components.forms

import app.components.Button
import japgolly.scalajs.react.vdom.VdomElement

object SubmitButton {
  def apply(name: String)(implicit formParams: FormCommonParams): VdomElement =
    Button(
      id = formParams.id + "-submit-btn",
      name = name,
      disabled = formParams.formData.hasErrors,
      onClick = formParams.submit
    )
}