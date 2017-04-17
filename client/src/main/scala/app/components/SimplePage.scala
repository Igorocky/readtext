package app.components

import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.pageparams.SimplePageParams
import upickle.default._

object SimplePage {
  protected type Props = SimplePageParams

  protected case class State(
                              str: String
                            )

  def apply(str: String): Unmounted[Props, State, Backend] = comp(read[Props](str))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState_P(p => State(p.param1))
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = <.div(s"Str = ${state.str}")
  }
}
