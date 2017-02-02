package app.components.forms

import app.Utils
import japgolly.scalajs.react._
import shared.forms.{PostData, _}
import shared.messages.Language

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

case class FormCommonParams(
                             id: String,
                             formData: FormData,
                             transformations: Map[String, InputTransformation[String, _]],
                             onChange: FormData => CallbackTo[FormData],
                             submitUrl: String,
                             language: Language,
                             beforeSubmit: Callback,
                             onSubmitSuccess: String => Callback,
                             onSubmitFormCheckFailure: Callback,
                             onAjaxError: Throwable => Callback,
                             editMode: Boolean = false
                           ) {

  lazy val submit: Callback = onChange(
    formData.validate(transformations)
  ) >>= { fd =>
    if (fd.hasErrors) {
      Callback.empty
    } else {
      beforeSubmit >> Callback.future {
        Utils.post(url = submitUrl, data = PostData.formSubmit(fd)).map {
          case Success(DataResponse(str)) => onSubmitSuccess(str)
          case Success(FormWithErrors(formData)) => onChange(formData) >> onSubmitFormCheckFailure
          case Failure(throwable) => onAjaxError(throwable)
        }
      }
    }
  }
}