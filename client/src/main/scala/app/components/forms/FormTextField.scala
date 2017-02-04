package app.components.forms

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react._
import org.scalajs.dom.raw.HTMLInputElement
import shared.SharedConstants._
import shared.messages.Language

object FormTextField {
  protected case class Props(name: String,
                             value: String,
                             errors: List[String],
                             onChange: String => CallbackTo[_],
                             width: Int,
                             editMode: Boolean,
                             onEnter: Callback)

  protected case class State(initialValue: String, value: String, focused: Boolean)

  def apply(name: String, width: Int = 150)
           (implicit formParams: FormCommonParams): ReactElement =
    comp(Props(
      name = name
      ,value = formParams.formData.get(name).value
      ,errors = formParams.formData.get(name).errors
      ,onChange = formParams.onChange compose formParams.formData.createSetter(name, formParams.transformations)
      ,width = width
      ,editMode = formParams.editMode
      ,onEnter = formParams.submit
    ))

  private val theInput: RefSimple[HTMLInputElement] = Ref[HTMLInputElement]("theInput")

  private class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = {
      <.div(
        if (state.focused) {
          <.input.text(
            ^.ref:=theInput,
            ^.name := props.name,
            ^.value := props.value,
            ^.onChange ==> { (e: ReactEventI) =>
              val newValue = e.target.value
              props.onChange(newValue)>>$.modState(_.copy(value = newValue))
            },
            ^.onBlur --> (if (props.editMode) $.modState(_.copy(focused = false)) else Callback.empty),
            ^.minWidth:=s"${props.width}px",
            ^.onKeyPress ==> { (e: ReactKeyboardEventI) =>
              if (e.charCode == 13) {
                props.onEnter
              } else {
                Callback.empty
              }
            }
          )
        } else {
          val valueIsEmpty = state.value.trim == ""
          <.div(
            ^.`class`:=EDITABLE_DIV +
              " " + (if (state.value != state.initialValue) EDITABLE_DIV_CHANGED else ""),
            ^.onClick --> $.modState(_.copy(focused = true), Callback{theInput($).tryFocus.runNow()}),
            ^.minWidth:=s"${props.width}px",
            if (valueIsEmpty) <.div(^.`class`:=EDITABLE_DIV_EMPTY, ".") else state.value
          )
        },
        props.errors.map(<.div(_))
      )
    }
  }

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(initialValue = p.value, value = p.value, focused = !p.editMode))
    .renderBackend[Backend]
    .build
}
