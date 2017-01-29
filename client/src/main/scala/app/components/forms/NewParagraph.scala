package app.components.forms

import app.components.Button
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactEventI, ReactKeyboardEventI}

object NewParagraph {

  protected case class Props(onOk: String => Callback, onCancel: Callback)

  protected case class State(title: String)

  def apply(onOk: String => Callback, onCancel: Callback) =
    comp(Props(onOk, onCancel))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState(State(""))
    .renderPS { ($, p, s) =>
      <.div(
        "New paragraph:",
        <.input.text(
          ^.value := s.title,
          ^.onChange ==> {(e: ReactEventI) =>
            val newValue = e.target.value
            $.modState(_.copy(title = newValue))
          },
          ^.onKeyPress ==> { (e: ReactKeyboardEventI) =>
            if (e.charCode == 13) {
              p.onOk(s.title)
            } else {
              Callback.empty
            }
          }
        ),
        Button(
          id = "new-paragraph-ok-btn",
          name = "Create",
          onClick = p.onOk(s.title)
        ),
        Button(
          id = "new-paragraph-cancel-btn",
          name = "Cancel",
          onClick = p.onCancel
        )
      )
    }.build
}