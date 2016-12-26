package app.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import shared.pageparams.SimplePageParams
import upickle.default._

object SimplePage {
  protected type Props = SimplePageParams

  protected case class State(
                              str: String
                            )

  def apply(str: String) = comp(read[Props](str))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(p.param1))
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = <.div(s"Str = ${state.str}")
  }
}
