package app.components.forms

import japgolly.scalajs.react
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, _}
import org.scalajs.dom.html
import shared.SharedConstants._
import shared.forms.FormField

object FormTextField {
  protected case class Props(focusOnMount: Boolean,
                             value: String,
                             errors: List[String],
                             onChange: String => CallbackTo[_],
                             width: Int,
                             editMode: Boolean,
                             onEnter: Callback,
                             placeholder: String,
                             onEscape: Callback)

  protected case class State(initialValue: String, focused: Boolean)

  def apply[T, S](field: FormField[T, String], width: Int = 150, placeholder: String = "", focusOnMount: Boolean = false, onEscape: Callback = Callback.empty)
                 (implicit formParams: FormCommonParams[T, S]) =
    comp(Props(
      focusOnMount = focusOnMount
      ,value = field.get(formParams.formData)
      ,errors = field.errors(formParams.formData)
      ,onChange = formParams.valueWasChanged(field)
      ,width = width
      ,editMode = formParams.editMode
      ,onEnter = formParams.submit
      ,placeholder = placeholder
      ,onEscape = onEscape
    ))

  protected class Backend($: BackendScope[Props, State]) {
    var theInput: html.Element = _

    def render(props: Props, state: State) = {
      <.div(
        if (props.errors.nonEmpty) ^.`class`:="has-error" else EmptyVdom,
        if (state.focused) {
          <.input.text.ref(theInput = _)(
            ^.`class`:="form-control",
            ^.placeholder:=props.placeholder,
            ^.value := props.value,
            ^.onChange ==> { (e: ReactEventFromInput) =>
              val newValue = e.target.value
              props.onChange(newValue).void
            },
            ^.onBlur --> (if (props.editMode) $.modState(_.copy(focused = false)) else Callback.empty),
            ^.maxWidth:=s"${props.width}px",
            ^.onKeyDown ==> { (e: ReactKeyboardEvent) =>
              if (e.keyCode == 13) {
                props.onEnter
              } else if (e.keyCode == 27) {
                props.onEscape
              } else {
                Callback.empty
              }
            }
          )
        } else {
          val valueIsEmpty = props.value.trim == ""
          <.div(
            ^.`class`:=EDITABLE_DIV +
              " " + (if (props.value != state.initialValue) EDITABLE_DIV_CHANGED else ""),
            ^.onClick --> $.modState(_.copy(focused = true), Callback(theInput.focus())),
            ^.minWidth:=s"${props.width}px",
            if (valueIsEmpty) <.div(^.`class`:=EDITABLE_DIV_EMPTY, ".") else props.value
          )
        },
        props.errors.toTagMod(<.div(^.color:="#a94442", _))
      )
    }
  }

  private lazy val comp = react.ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(initialValue = p.value, focused = !p.editMode))
    .renderBackend[Backend]
    .componentDidMount{$ => if ($.props.focusOnMount) Callback($.backend.theInput.focus()) else Callback.empty}
    .build
}
