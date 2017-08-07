package app.components.learncards

import app.Utils._
import app.components.WindowFunc
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.dto.EasinessLevels._
import shared.dto.ScoreLevels._
import shared.dto.TopicHistoryRecordUtils._
import shared.dto.{Easiness, Score}

object LearnCardCmp {

  case class Props(key: String,
                   ctx: WindowFunc,
                   question: TagMod,
                   answer: TagMod,
                   scoreSelected: (Easiness, Score) => Callback
                   ) {
    @inline def render = comp.withKey(key)(this)
  }

  protected case class State(easiness: Option[Easiness] = None)

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
      easinessButton(HARD),easinessButton(MEDIUM),easinessButton(EASY)
    )

    def selectScoreButtons(implicit p: Props, s: State) = <.div(
      scoreButton(BAD),scoreButton(POOR),scoreButton(GOOD),scoreButton(EXCELLENT)
    )

    def easinessButton(easiness: Easiness) = buttonWithText(
      onClick = $.modState(_.copy(easiness = Some(easiness))),
      btnType = easiness match {
        case EASY => BTN_SUCCESS
        case MEDIUM => BTN_WARNING
        case HARD => BTN_DANGER
      },
      text = easinessStr(easiness)
    )

    def scoreButton(score: Score)(implicit p: Props, s: State) = buttonWithText(
      onClick = p.scoreSelected(s.easiness.get, score),
      btnType = score match {
        case BAD => BTN_DANGER
        case POOR => BTN_WARNING
        case GOOD => BTN_INFO
        case EXCELLENT => BTN_SUCCESS
      },
      text = scoreStr(score)
    )


  }
}