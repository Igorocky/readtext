package app.components.listtopics

import app.Utils.{BTN_DEFAULT, buttonWithText}
import app.components.WindowFunc
import app.components.forms.FormCommonParams.SubmitFunction
import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import org.scalajs.dom.raw.File
import shared.dto.Topic
import shared.forms.{FormData, Forms}
import shared.messages.Language

object TopicForm {

  case class Props(ctx: WindowFunc,
                   topic: Topic,
                   cancelled: Callback,
                   submitFunction: SubmitFunction[Topic, Topic],
                   submitComplete: Topic => Callback,
                   textFieldLabel: String,
                   submitButtonName: String,
                   editMode: Boolean = false,
                   language: Language,
                   uploadTopicFileUrl: String,
                   getTopicImgUrl: String,
                   unregisterPasteListener: Long => Callback,
                   registerPasteListener: (Long, File => Callback) => Callback) {
    @inline def render = comp(this)
  }

  protected case class State(formData: FormData[Topic])

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(formData = FormData(p.language, p.topic)))
    .renderPS { ($, props, state) =>
      val formMethods = Forms.topicForm
      implicit val fParams = FormCommonParams[Topic, Topic](
        formMethods = formMethods,
        formData = state.formData,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        beforeSubmit = props.ctx.openWaitPane,
        submitFunction = props.submitFunction,
        onSubmitSuccess = topic => props.ctx.closeWaitPane >> props.submitComplete(topic),
        onSubmitFormCheckFailure = props.ctx.closeWaitPane,
        editMode = false
      )
      <.div(
        ^.`class` := this.getClass.getSimpleName + " form",
        props.textFieldLabel,
        FormTextField(field = formMethods.title, focusOnMount = !props.editMode, width = 700, placeholder = "Topic Title"),
        if (props.editMode) ImgUploader(
          props.ctx,
          props.topic,
          field = formMethods.images,
          uploadTopicFileUrl = props.uploadTopicFileUrl,
          getTopicImgUrl = props.getTopicImgUrl,
          unregisterPasteListener = props.unregisterPasteListener,
          registerPasteListener = props.registerPasteListener
        ) else EmptyVdom,
        SubmitButton(props.submitButtonName),
        buttonWithText(
          onClick = props.cancelled,
          btnType = BTN_DEFAULT,
          text = "Cancel"
        )
      )
    }.componentWillReceiveProps { $ =>
    if ($.nextProps.language != $.state.formData.language) {
      $.modState(_.copy(
        formData = Forms.topicForm.changeLang($.nextProps.language, $.state.formData)
      ))
    } else {
      Callback.empty
    }
  }.build
}
