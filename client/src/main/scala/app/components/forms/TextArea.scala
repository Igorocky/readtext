package app.components.forms

import japgolly.scalajs.react.{ScalaComponent, _}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html
import shared.SharedConstants._
import shared.messages.Language

object TextArea {
  protected case class Props(name: String,
                             value: String,
                             errors: List[String],
                             onChange: String => CallbackTo[_],
                             width: Int,
                             rows: Int,
                             editMode: Boolean)

  protected case class State(initialValue: String, value: String, focused: Boolean)

  def apply(name: String, width: Int = 150, rows: Int = 10)
           (implicit formParams: FormCommonParams, language: Language): VdomElement =
    comp(Props(
      name = name
      ,value = formParams.formData.get(name).value
      ,errors = formParams.formData.get(name).errors
      ,onChange = formParams.onChange compose formParams.formData.createSetter(name, formParams.transformations)
      ,width = width
      ,rows = rows
      ,editMode = formParams.editMode
    ))

  private class Backend($: BackendScope[Props, State]) {
    private var theInput: html.Element = _

    def render(props: Props, state: State) = {
      <.div(
        if (state.focused) {
          <.textarea.ref(theInput = _)(
            ^.name := props.name,
            ^.value := props.value,
            ^.onChange ==> { (e: ReactEventFromInput) =>
              val newValue = e.target.value
              props.onChange(newValue)>>$.modState(_.copy(value = newValue))
            },
            ^.onBlur --> (if (props.editMode) $.modState(_.copy(focused = false)) else Callback.empty),
            ^.minWidth:=s"${props.width}px",
            ^.rows:=props.rows
          )
        } else {
          val valueIsEmpty = state.value.trim == ""
          <.div(
            ^.`class`:=EDITABLE_DIV +
              " " + (if (state.value != state.initialValue) EDITABLE_DIV_CHANGED else ""),
            ^.onClick --> $.modState(_.copy(focused = true), Callback(theInput.focus())),
            ^.minWidth:=s"${props.width}px",
            if (valueIsEmpty) <.div(^.`class`:=EDITABLE_DIV_EMPTY, ".") else state.value
          )
        },
        props.errors.toTagMod(<.div(_))
      )
    }
  }

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState_P(p => State(initialValue = p.value, value = p.value, focused = !p.editMode))
    .renderBackend[Backend]
    .build
}
