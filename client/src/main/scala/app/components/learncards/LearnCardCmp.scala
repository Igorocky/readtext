package app.components.learncards

import app.Utils._
import app.components.WindowFunc
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}

object LearnCardCmp {

  case class Props(key: String,
                   ctx: WindowFunc,
                   question: TagMod,
                   answer: TagMod,
                   scoreSelected: (Int/*easiness*/, Int/*score*/) => Callback
                   ) {
    @inline def render = comp.withKey(key)(this)
  }

  protected case class State(easiness: Option[Int] = None)

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
        if (s.easiness.isEmpty) {
          selectEasinessButtons
        } else {
          TagMod(
            selectScoreButtons,
            <.div(
              p.answer
            )
          )
        }
      )
    )

    def selectEasinessButtons(implicit p: Props) = <.div(
      easinessButton(0),easinessButton(1),easinessButton(2)
    )

    def selectScoreButtons(implicit p: Props, s: State) = <.div(
      scoreButton(0),scoreButton(1),scoreButton(2),scoreButton(3)
    )

    def easinessButton(level: Int) = buttonWithText(
      onClick = $.modState(_.copy(easiness = Some(level))),
      btnType = level match {
        case 2 => BTN_SUCCESS
        case 1 => BTN_WARNING
        case 0 => BTN_DANGER
      },
      text = level match {
        case 2 => "Easy"
        case 1 => "Middle"
        case 0 => "Hard"
      }
    )

    def scoreButton(score: Int)(implicit p: Props, s: State) = buttonWithText(
      onClick = p.scoreSelected(s.easiness.get, score),
      btnType = score match {
        case 0 => BTN_DANGER
        case 1 => BTN_WARNING
        case 2 => BTN_INFO
        case 3 => BTN_SUCCESS
      },
      text = score match {
        case 0 => "Bad"
        case 1 => "Poor"
        case 2 => "Good"
        case 3 => "Excellent"
      }
    )


  }
}