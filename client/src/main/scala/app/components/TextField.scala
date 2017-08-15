package app.components

import japgolly.scalajs.react
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, _}
import org.scalajs.dom.html

object TextField {
  case class Props(focusOnMount: Boolean = false,
                             value: String = "",
                             onChange: String => CallbackTo[_] = _ => Callback.empty,
                             width: Int = 150,
                             placeholder: String = "",
                             onEnter: String => CallbackTo[String] = str => Callback.empty.map(_ => str),
                             onEscape: String => CallbackTo[String] = str => Callback.empty.map(_ => str),
                             key: String = "TextField") {
    @inline def render = comp.withKey(key)(this)
  }

  protected case class State(curValue: String)

  protected class Backend($: BackendScope[Props, State]) {
    var theInput: html.Element = _

    def render(props: Props, state: State) = {
          <.input.text.ref(theInput = _)(
            ^.`class`:="form-control",
            ^.placeholder:=props.placeholder,
            ^.value := state.curValue,
            ^.onChange ==> { (e: ReactEventFromInput) =>
              val newValue = e.target.value
              $.modState(_.copy(curValue = newValue)) >> props.onChange(newValue).void
            },
            ^.maxWidth:=s"${props.width}px",
            ^.onKeyDown ==> { (e: ReactKeyboardEvent) =>
              if (e.keyCode == 13) {
                props.onEnter(state.curValue) >>= (newVal => $.modState(_.copy(curValue = newVal)))
              } else if (e.keyCode == 27) {
                props.onEscape(state.curValue) >>= (newVal => $.modState(_.copy(curValue = newVal)))
              } else {
                Callback.empty
              }
            }
          )
    }
  }

  private lazy val comp = react.ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(curValue = p.value))
    .renderBackend[Backend]
    .componentDidMount{$ => if ($.props.focusOnMount) Callback($.backend.theInput.focus()) else Callback.empty}
    .build
}
