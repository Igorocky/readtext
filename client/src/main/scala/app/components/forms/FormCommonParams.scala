package app.components.forms

import japgolly.scalajs.react._
import org.scalajs.dom.ext.Ajax
import shared.forms.PostDataTypes.DATA_RESPONSE
import shared.forms.{FormData, InputTransformation, PostData}
import shared.messages.Language
import upickle.default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

case class FormCommonParams(
                             formData: FormData,
                             transformations: Map[String, InputTransformation[String, _]],
                             onChange: FormData => CallbackTo[FormData],
                             submitUrl: String,
                             language: Language,
                             beforeSubmit: Callback,
                             onSubmitSuccess: String => Callback,
                             onSubmitFailure: Callback,
                             editMode: Boolean = false
                           ) {

  lazy val submit: Callback = onChange(
    formData.validate(transformations, language)
  ) >>= { fd =>
    if (fd.hasErrors) {
      Callback.empty
    } else {
      beforeSubmit >> Callback.future {
        Ajax.post(
          url = submitUrl,
          data = PostData.forSubmit(fd)
        ).map { resp =>
          val postData = PostData.readPostData(resp.responseText)
          if (postData.typ == DATA_RESPONSE) {
            onSubmitSuccess(postData.content)
          } else {
            onChange(
              read[FormData](postData.content)
            ) >> onSubmitFailure
          }
        }
      }
    }
  }
}