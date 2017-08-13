package app.components.learncards

import app.components.WindowFunc
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}

object LearnCardCmp {

  case class Props(key: String,
                   ctx: WindowFunc,
                   question: TagMod,
                   answer: TagMod,
                   scoreSelected: String => Callback
                   ) {
    @inline def render = comp.withKey(key)(this)
  }

  protected case class State(answerIsHidden: Boolean = true)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(implicit p: Props, s: State) = <.div(^.`class`:=LearnCardCmp.getClass.getSimpleName,
      <.div(^.key:="question",
        p.question
      ),
      <.div(^.key:="answer-block",
        if (!s.answerIsHidden) {
          TagMod(
            EmptyVdom,
            <.div(
              p.answer
            )
          )
        } else {
          EmptyVdom
        }
      )
    )


  }
}