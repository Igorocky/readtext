package app.components.listtopics

import app.Utils._
import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import shared.forms.{FormData, FormKeys, Forms}

object ParagraphForm {

  protected case class Props(globalScope: ListTopicsPageGlobalScope,
                             formData: FormData,
                             cancelled: Callback,
                             submitComplete: String => Callback,
                             textFieldLabel: String,
                             submitButtonName: String,
                             editMode: Boolean = false)

  protected case class State(formData: FormData)

  def apply(globalScope: ListTopicsPageGlobalScope,
            formData: FormData,
            cancelled: Callback,
            submitComplete: String => Callback,
            textFieldLabel: String,
            submitButtonName: String,
            editMode: Boolean = false) =
    comp(Props(
      globalScope,
      formData,
      cancelled,
      submitComplete,
      textFieldLabel,
      submitButtonName,
      editMode
    ))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(props => State(formData = props.formData))
    .renderPS{($,props,state)=>
      implicit val lang = props.globalScope.language
      implicit val fParams = FormCommonParams(
        id = "paragraph-form",
        formData = state.formData,
        transformations = Forms.paragraphForm.transformations,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        beforeSubmit = props.globalScope.openWaitPane,
        onSubmitSuccess = str => props.globalScope.closeWaitPane >> props.submitComplete(str),
        onSubmitFormCheckFailure = props.globalScope.closeWaitPane,
        onAjaxError = th => props.globalScope.openOkDialog(s"""Error: ${th.getMessage}"""),
        editMode = props.editMode
      )
      <.div(
        ^.`class`:=this.getClass.getSimpleName + " form",
        props.textFieldLabel,
        FormTextField(key = FormKeys.TITLE, focusOnMount = true, width = 700, placeholder = "Paragraph Title"),
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
            .validate(Forms.paragraphForm.transformations)
        ))
      } else {
        Callback.empty
      }
    }.build
}
