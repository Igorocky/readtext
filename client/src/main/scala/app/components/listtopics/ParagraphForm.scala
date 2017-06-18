package app.components.listtopics

import app.Utils._
import app.components.forms.FormCommonParams.SubmitFunction
import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import shared.dto.Paragraph
import shared.forms.{FormData, Forms}

object ParagraphForm {

  case class Props(globalScope: ListTopicsPageGlobalScope,
                   paragraph: Paragraph,
                   submitFunction: SubmitFunction[Paragraph, Paragraph],
                   cancelled: Callback,
                   submitComplete: Paragraph => Callback,
                   textFieldLabel: String,
                   submitButtonName: String,
                   editMode: Boolean = false) {
    @inline def render = comp(this)
  }

  protected case class State(formData: FormData[Paragraph])

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(props => State(formData = FormData(props.globalScope.language, props.paragraph)))
    .renderPS{($,props,state)=>
      implicit val lang = props.globalScope.language
      val formMethods = Forms.paragraphForm
      implicit val fParams = FormCommonParams[Paragraph, Paragraph](
        formMethods = formMethods,
        formData = state.formData,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        beforeSubmit = props.globalScope.openWaitPane,
        submitFunction = props.submitFunction,
        onSubmitSuccess = props.submitComplete,
        onSubmitFormCheckFailure = props.globalScope.closeWaitPane,
        editMode = props.editMode
      )
      <.div(
        ^.`class`:=this.getClass.getSimpleName + " form",
        props.textFieldLabel,
        FormTextField(field = formMethods.title, focusOnMount = true, width = 700, placeholder = "Paragraph Title"),
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
          formData = Forms.paragraphForm.changeLang($.nextProps.globalScope.language, $.state.formData)
        ))
      } else {
        Callback.empty
      }
    }.build
}
