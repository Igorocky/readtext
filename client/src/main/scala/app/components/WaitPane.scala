package app.components

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import shared.SharedConstants._

object WaitPane {
  protected case class Props(absolute: Boolean)

  def apply(absolute: Boolean = false) = comp(Props(absolute))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P {props =>
      <.div(^.`class`:= (if (props.absolute) WAIT_PANE_ABSOLUTE else WAIT_PANE_FIXED))
    }.build
}