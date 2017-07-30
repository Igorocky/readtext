package app.components

import japgolly.scalajs.react
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.html_<^._

object Checkbox {
  type NewValue = Boolean

  case class Props(key: Option[String] = None,
                             checked: Boolean = false,
                             onChange: NewValue => Callback = _ => Callback.empty,
                             disabled: Boolean = false) {
    @inline def render = if (key.isDefined) comp.withKey(key.get)(this) else comp(this)
  }

  private lazy val comp = react.ScalaComponent.builder[Props](this.getClass.getName)
    .render_P { props =>
      <.input(
        ^.`type`:="checkbox",
        ^.checked:=props.checked,
         ^.disabled := props.disabled,
        ^.onChange --> props.onChange(!props.checked)
      )
    }.build
}