package app.components

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import shared.SharedConstants._

object ModalDialog {
  protected case class Props(width: String, content: VdomElement)

  def apply(width: String, content: VdomElement) =
    comp(Props(width, content))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .render_P { props =>
      <.div(^.`class`:=MODAL_DIAG_BACK_PANE,
        <.div(^.`class`:=MODAL_DIAG_CONTENT, ^.width:=props.width + "px",
          props.content
        )
      )
    }.build
}