package app.components.forms

import japgolly.scalajs.react
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._

object Hidden {
  protected case class Props(name: String, value: String)

  def apply(name: String, value: String) = comp(Props(name = name, value = value))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .render_P { props =>
      <.input.hidden(
        ^.name:=props.name,
        ^.value:=props.value
      )
    }.build
}