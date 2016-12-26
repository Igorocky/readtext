package app.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, ReactElement}
import shared.SharedConstants._

object ModalDialog {
  protected case class Props(width: String, content: ReactElement)

  def apply(width: String, content: ReactElement) =
    comp(Props(width, content))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { props =>
      <.div(^.`class`:=MODAL_DIAG_BACK_PANE,
        <.div(^.`class`:=MODAL_DIAG_CONTENT, ^.width:=props.width,
          props.content
        )
      )
    }.build
}