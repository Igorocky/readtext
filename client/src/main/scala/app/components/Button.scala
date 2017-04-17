package app.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}

object Button {
  protected case class Props(name: String, onClick: Callback, disabled: Boolean, clazz: Option[String])

  def apply(id: String, name: String,
            onClick: Callback, disabled: Boolean = false, clazz: Option[String] = None): VdomElement =
    comp.withKey(id)(Props(name = name, onClick = onClick, disabled = disabled, clazz = clazz))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .render_P { props =>
      <.button(
        ^.`type`:="button",
         ^.disabled := props.disabled,
        ^.onClick --> props.onClick,
        props.clazz.whenDefined(cl => ^.`class`:=cl),
        props.name
      )
    }.build
}