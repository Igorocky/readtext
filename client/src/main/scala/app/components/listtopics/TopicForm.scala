package app.components.listtopics

import app.Utils.{BTN_DEFAULT, buttonWithText}
import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.FormKeys
import shared.dto.Topic
import shared.forms.{FormData, Forms}

object TopicForm {
  protected case class Props(globalScope: GlobalScope,
                             formData: FormData,
                             topic: Option[Topic] = None,
                             cancelled: Callback,
                             submitComplete: String => Callback,
                             textFieldLabel: String,
                             submitButtonName: String,
                             editMode: Boolean = false)

  protected case class State(formData: FormData)

  def apply(globalScope: GlobalScope,
            formData: FormData,
            topic: Option[Topic] = None,
            cancelled: Callback,
            submitComplete: String => Callback,
            textFieldLabel: String,
            submitButtonName: String,
            editMode: Boolean = false) =
    comp(Props(
      globalScope,
      formData,
      topic,
      cancelled,
      submitComplete,
      textFieldLabel,
      submitButtonName,
      editMode
    ))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState_P(p => State(formData = p.formData))
    .renderPS{($,props,state)=>
      implicit val fParams = FormCommonParams(
        id = "topic-form",
        formData = state.formData,
        transformations = Forms.topicForm.transformations,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        submitUrl = state.formData.submitUrl,
        beforeSubmit = props.globalScope.openWaitPane,
        onSubmitSuccess = str => props.globalScope.closeWaitPane >> props.submitComplete(str),
        onSubmitFormCheckFailure = props.globalScope.closeWaitPane,
        onAjaxError = th => props.globalScope.openOkDialog(s"""Error: ${th.getMessage}"""),
        editMode = false
      )
      <.div(
        ^.`class`:=this.getClass.getSimpleName + " form",
        props.textFieldLabel,
        FormTextField(key = FormKeys.TITLE, focusOnMount = !props.editMode, width = 700, placeholder = "Topic Title"),
        if (props.editMode) ImgUploader(
          props.globalScope,
          props.topic.get,
          key = FormKeys.IMAGES
        ) else EmptyVdom,
        SubmitButton(props.submitButtonName),
        buttonWithText(
          onClick = props.cancelled,
          btnType = BTN_DEFAULT,
          text = "Cancel"
        )
      )
    }.componentWillReceiveProps{$=>
      if ($.nextProps.globalScope.language != $.state.formData.language) {
        $.modState(_.copy(
          formData = $.state.formData
            .copy(language = $.nextProps.globalScope.language)
            .validate(Forms.topicForm.transformations)
        ))
      } else {
        Callback.empty
      }
    }.build
}
