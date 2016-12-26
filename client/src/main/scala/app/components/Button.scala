package app.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}

object Button {
  protected case class Props(name: String, onClick: Callback, disabled: Boolean)

  def apply(name: String, onClick: Callback, disabled: Boolean = false) =
    comp(Props(name = name, onClick = onClick, disabled = disabled))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { props =>
      <.button(
        ^.`type`:="button",
         ^.disabled := props.disabled,
        ^.onClick --> props.onClick,
        props.name
      )
    }.build
}