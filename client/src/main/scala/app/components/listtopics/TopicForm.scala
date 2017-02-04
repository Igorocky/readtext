package app.components.listtopics

import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import app.components.{Button, WaitPane}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.SharedConstants
import shared.dto.Topic
import shared.forms.{FormData, Forms}
import upickle.default._

object TopicForm {
  protected case class Props(globalScope: GlobalScope,
                             formData: FormData,
                             topic: Option[Topic] = None,
                             cancelled: Callback,
                             submitComplete: Topic => Callback,
                             textFieldLabel: String,
                             submitButtonName: String,
                             editMode: Boolean = false)

  protected case class State(formData: FormData)

  def apply(globalScope: GlobalScope,
            formData: FormData,
            topic: Option[Topic] = None,
            cancelled: Callback,
            submitComplete: Topic => Callback,
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

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(formData = p.formData))
    .renderPS{($,props,state)=>
      implicit val fParams = FormCommonParams(
        id = "topic-form",
        formData = state.formData,
        transformations = Forms.topicForm.transformations,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        submitUrl = state.formData.submitUrl,
        beforeSubmit = props.globalScope.openWaitPane,
        onSubmitSuccess = str => props.globalScope.closeWaitPane >> props.submitComplete(read[Topic](str)),
        onSubmitFormCheckFailure = props.globalScope.closeWaitPane,
        onAjaxError = th => props.globalScope.openOkDialog(s"""Error: ${th.getMessage}"""),
        editMode = false
      )
      <.div(
        <.div(if (state.formData.hasErrors) "There are errors" else ""),
        props.textFieldLabel,
        FormTextField(SharedConstants.TITLE),
        if (props.editMode) ImgUploader(
          props.globalScope,
          props.topic.get,
          name = SharedConstants.IMAGES
        ) else EmptyTag,
        SubmitButton(props.submitButtonName),
        Button(id = "topic-form-cancel-btn", name = "Cancel", onClick = props.cancelled)
      )
    }.componentWillReceiveProps{$=>
      if ($.nextProps.globalScope.language != $.currentState.formData.language) {
        $.$.modState(_.copy(
          formData = $.currentState.formData
            .copy(language = $.nextProps.globalScope.language)
            .validate(Forms.textForm.transformations)
        ))
      } else {
        Callback.empty
      }
    }.build
}
