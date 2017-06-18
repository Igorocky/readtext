package app.components.forms

import app.components.forms.FormCommonParams.SubmitFunction
import japgolly.scalajs.react.{Callback, _}
import shared.forms.Forms.SubmitResponse
import shared.forms._

object FormCommonParams {
  type SubmitFunction[F,S] = F => (SubmitResponse[F,S] => Callback) => Callback
}

case class FormCommonParams[T, S](
                                   formMethods: FormMethods[T],
                                   formData: FormData[T],
                                   onChange: FormData[T] => CallbackTo[FormData[T]],
                                   beforeSubmit: Callback,
                                   submitFunction: SubmitFunction[T,S],
                                   onSubmitSuccess: S => Callback,
                                   onSubmitFormCheckFailure: Callback,
                                   editMode: Boolean = false
                           ) {

  lazy val submit: Callback = onChange(formMethods.validate(formData)) >>= { fd =>
    if (fd.hasErrors) {
      Callback.empty
    } else {
      beforeSubmit >> submitFunction(fd.data){
        case Left(newFormData) => onChange(newFormData) >> onSubmitFormCheckFailure
        case Right(obj) => onSubmitSuccess(obj)
      }
    }
  }

  def valueWasChanged[F](field: FormField[T, F])(newValue: F): Callback = onChange(field.setAndValidate(newValue, formData)).void
}
