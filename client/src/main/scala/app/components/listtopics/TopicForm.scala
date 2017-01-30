package app.components.listtopics

import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import app.components.{Button, WaitPane}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.SharedConstants
import shared.dto.Topic
import shared.forms.{FormData, Forms}
import shared.messages.Language
import upickle.default._

object TopicForm {
  protected case class Props(
                              language: Language,
                              formData: FormData,
                              cancelled: Callback,
                              submitComplete: Topic => Callback,
                              textFieldTitle: String,
                              submitButtonName: String,
                              editMode: Boolean = false)

  protected case class State(langOfFormData: Language, formData: FormData, waitPaneOpened: Boolean)

  def apply(
             language: Language,
             formData: FormData,
             cancelled: Callback,
             submitComplete: Topic => Callback,
             textFieldTitle: String,
             submitButtonName: String,
             editMode: Boolean = false) =
    comp(Props(language, formData, cancelled, submitComplete, textFieldTitle, submitButtonName, editMode))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(langOfFormData = p.language, formData = p.formData, waitPaneOpened = false))
    .renderPS{($,props,state)=>
      val openWaitPane = $.modState(_.copy(waitPaneOpened = true))
      val closeWaitPane = $.modState(_.copy(waitPaneOpened = false))
      implicit val lang = props.language
      implicit val fParams = FormCommonParams(
        id = "topic-form",
        formData = state.formData,
        transformations = Forms.topicForm.transformations,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        submitUrl = state.formData.submitUrl,
        language = lang,
        beforeSubmit = openWaitPane,
        onSubmitSuccess = str => closeWaitPane >> props.submitComplete(read[Topic](str)),
        onSubmitFormCheckFailure = closeWaitPane,
        editMode = props.editMode
      )
      <.div(
        <.div(if (state.formData.hasErrors) "There are errors" else ""),
        props.textFieldTitle,
        FormTextField(SharedConstants.TITLE),
        FileUploader(),
        SubmitButton(props.submitButtonName),
        Button(id = "topic-form-cancel-btn", name = "Cancel", onClick = props.cancelled),
        if (state.waitPaneOpened) WaitPane() else EmptyTag
      )
    }.componentWillReceiveProps{$=>
      if ($.nextProps.language != $.currentState.langOfFormData) {
        $.$.modState(_.copy(
          langOfFormData = $.nextProps.language,
          formData = $.currentState.formData.validate(Forms.textForm.transformations, $.nextProps.language)
        ))
      } else {
        Callback.empty
      }
    }.build
}
