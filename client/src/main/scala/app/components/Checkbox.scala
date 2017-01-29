package app.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}

object Checkbox {
  type NewValue = Boolean

  protected case class Props(checked: Boolean, onChange: NewValue => Callback, disabled: Boolean)

  def apply(id: String, checked: Boolean, onChange: NewValue => Callback, disabled: Boolean = false) =
    comp.withKey(id)(Props(checked = checked, onChange = onChange, disabled = disabled))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { props =>
      <.input(
        ^.`type`:="checkbox",
        ^.checked:=props.checked,
         ^.disabled := props.disabled,
        ^.onChange --> props.onChange(!props.checked)
      )
    }.build
}