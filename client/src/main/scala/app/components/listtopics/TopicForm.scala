package app.components.listtopics

import app.Utils.{BTN_DEFAULT, buttonWithText}
import app.components.forms.FormCommonParams.SubmitFunction
import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.dto.Topic
import shared.forms.{FormData, Forms}

object TopicForm {

  case class Props(globalScope: ListTopicsPageGlobalScope,
                   topic: Topic,
                   cancelled: Callback,
                   submitFunction: SubmitFunction[Topic, Topic],
                   submitComplete: Topic => Callback,
                   textFieldLabel: String,
                   submitButtonName: String,
                   editMode: Boolean = false) {
    @inline def render = comp(this)
  }

  protected case class State(formData: FormData[Topic])

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(formData = FormData(p.globalScope.language, p.topic)))
    .renderPS { ($, props, state) =>
      val formMethods = Forms.topicForm
      implicit val fParams = FormCommonParams[Topic, Topic](
        formMethods = formMethods,
        formData = state.formData,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        beforeSubmit = props.globalScope.openWaitPane,
        submitFunction = props.submitFunction,
        onSubmitSuccess = topic => props.globalScope.closeWaitPane >> props.submitComplete(topic),
        onSubmitFormCheckFailure = props.globalScope.closeWaitPane,
        editMode = false
      )
      <.div(
        ^.`class` := this.getClass.getSimpleName + " form",
        props.textFieldLabel,
        FormTextField(field = formMethods.title, focusOnMount = !props.editMode, width = 700, placeholder = "Topic Title"),
        if (props.editMode) ImgUploader(
          props.globalScope,
          props.topic,
          field = formMethods.images
        ) else EmptyVdom,
        SubmitButton(props.submitButtonName),
        buttonWithText(
          onClick = props.cancelled,
          btnType = BTN_DEFAULT,
          text = "Cancel"
        )
      )
    }.componentWillReceiveProps { $ =>
    if ($.nextProps.globalScope.language != $.state.formData.language) {
      $.modState(_.copy(
        formData = Forms.topicForm.changeLang($.nextProps.globalScope.language, $.state.formData)
      ))
    } else {
      Callback.empty
    }
  }.build
}
