package app.components

import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.SharedConstants
import shared.forms.{FormData, Forms}
import shared.messages.{Language, Messages}
import shared.pageparams.TextUI
import upickle.default._

object TextForm {
  protected case class Props(
                              language: Language,
                              formData: FormData,
                              cancelled: Callback,
                              submitComplete: TextUI => Callback,
                              submitButtonName: String,
                              editMode: Boolean = false)

  protected case class State(langOfFormData: Language, formData: FormData, waitPaneOpened: Boolean)

  def apply(
             language: Language,
             formData: FormData,
             cancelled: Callback,
             submitComplete: TextUI => Callback,
             submitButtonName: String,
             editMode: Boolean = false) =
    comp(Props(language, formData, cancelled, submitComplete, submitButtonName, editMode))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(langOfFormData = p.language, formData = p.formData, waitPaneOpened = false))
    .renderPS{($,props,state)=>
      implicit val lang = props.language
      implicit val fParams = FormCommonParams(
        formData = state.formData,
        transformations = Forms.textFrom.transformations,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        submitUrl = state.formData.submitUrl,
        language = lang,
        beforeSubmit = $.modState(_.copy(waitPaneOpened = true)),
        onSubmitSuccess = str => props.submitComplete(read[TextUI](str)),
        onSubmitFailure = $.modState(_.copy(waitPaneOpened = false)),
        editMode = props.editMode
      )
      <.div(
        <.div(
          Messages.title
        ),
        <.div(
          FormTextField(SharedConstants.TITLE)
        ),
        <.div(
          Messages.content
        ),
        <.div(
          FormTextField(SharedConstants.CONTENT)
        ),
        <.div(if (state.formData.hasErrors) "There are errors" else ""),
        SubmitButton(props.submitButtonName),
        Button(name = "Cancel", onClick = props.cancelled),
        if (state.waitPaneOpened) WaitPane() else EmptyTag
      )
    }.componentWillReceiveProps{$=>
      if ($.nextProps.language != $.currentState.langOfFormData) {
        $.$.modState(_.copy(
          langOfFormData = $.nextProps.language,
          formData = $.currentState.formData.validate(Forms.textFrom.transformations, $.nextProps.language)
        ))
      } else {
        Callback.empty
      }
    }.build
}
