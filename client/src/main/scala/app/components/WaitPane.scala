package app.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.SharedConstants._

object WaitPane {
  protected case class Props(bgColor: String, absolute: Boolean)

  def apply(bgColor: String = "rgba(80,80,80,0.5)", absolute: Boolean = false) = comp(Props(bgColor, absolute))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .render_P {props =>
      <.div(
        ^.`class`:= (if (props.absolute) WAIT_PANE_ABSOLUTE else WAIT_PANE_FIXED),
        ^.backgroundColor:=props.bgColor
      )
    }.build
}