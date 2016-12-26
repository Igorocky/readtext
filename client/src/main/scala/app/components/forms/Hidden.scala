package app.components.forms

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Hidden {
  protected case class Props(name: String, value: String)

  def apply(name: String, value: String) = comp(Props(name = name, value = value))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { props =>
      <.input.hidden(
        ^.name:=props.name,
        ^.value:=props.value
      )
    }.build
}