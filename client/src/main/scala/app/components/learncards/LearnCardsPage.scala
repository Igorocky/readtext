package app.components.learncards

import app.Utils
import app.Utils.{BTN_INFO, buttonWithText}
import app.components._
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import shared.SharedConstants._
import shared.dto.TopicHistoryRecordUtils._
import shared.dto.{Easiness, Score, TopicState}
import shared.pageparams.LearnCardsPageParams
import upickle.default._

object LearnCardsPage {
  protected type Props = LearnCardsPageParams

  protected type State = LearnCardsPageState

  def apply(str: String): Unmounted[Props, State, Backend] = comp(read[Props](str))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => LearnCardsPageState(pageParams = p))
    .renderBackend[Backend]
    .componentWillMount { $ =>
      $.modState(
        _.copy(
          modState = $.modState(_),
          getState = () => $.state,
          wsClient = Utils.createWsClient($.props.wsEntryUrl),
          sessionWsClient = Utils.createWsClient($.props.wsEntryUrl)
        ),
        CallbackTo($.state) >>= (_.loadCardStates)
      )
    }
    .build

  protected class Backend($: BackendScope[Props, State]) {

    def render(implicit s: State) = UnivPage.Props(
      language = s.pageParams.headerParams.language,
      sessionWsClient = s.sessionWsClient,
      onLanguageChange = newLang => $.modState(_.setLanguage(newLang)),
      windowFuncMem = s.windowFuncMem,
      windowFunc = s,
      content = (if (s.learnCardsPageMem.topicStates.isDefined) {
        <.div(
          <.div(s"Number of topics: ${s.learnCardsPageMem.topicStates.get.size}, Average score: ${s.learnCardsPageMem.avgScore}"),
          <.table(^.`class`:=TOPIC_STATUSES_TABLE,
            <.thead(
              <.th("#"),<.th("Duration"),<.th("Easiness"),<.th("Score"),<.th(""),<.th("Last learned")
            ),
            <.tbody(
              s.learnCardsPageMem.topicStates.get.zipWithIndex.toVdomArray{
                case (TopicState(id, easiness, score, Some(time), duration), idx) => <.tr(^.key:=id.toString,
                  <.td(idx.toString),
                  <.td(duration),
                  easinessTd(easiness),
                  scoreTd(score),
                  <.td(selectButton(id)),
                  <.td(time)
                )
                case (TopicState(id, _, _, None, _), idx) => <.tr(^.key:=id.toString,
                  <.td(idx.toString),
                  <.td(""),
                  <.td(""),
                  <.td(""),
                  <.td(selectButton(id)),
                  <.td("")
                )
              }
            )
          )
        )
      } else if (s.learnCardsPageMem.topic.isDefined) {
        LearnCardCmp.Props(
          key = s.learnCardsPageMem.topic.get.id.get.toString,
          ctx = s,
          question = <.h2(s.learnCardsPageMem.topic.get.title),
          answer = <.div(s.learnCardsPageMem.topic.get.images.toVdomArray { img =>
            <.div(^.key:= img,
              <.img(^.src := s.pageParams.getTopicImgUrl + "/" + s.learnCardsPageMem.topic.get.id.get + "/" + img)
            )
          }),
          scoreSelected = s.scoreSelected(s.learnCardsPageMem.topic.get.id.get)
        ).render
      } else {
        "Loading..."
      })
    ).render


    def selectButton(topicId: Long)(implicit s: State) = buttonWithText(
      onClick = s.topicSelected(topicId),
      btnType = BTN_INFO,
      text = "Select"
    )

    def easinessTd(easiness: Easiness) = <.td(^.`class`:=easinessClass(easiness) + " " + EASINESS_SCORE,
      easinessStr(easiness)
    )

    def scoreTd(score: Score) = <.td(^.`class`:=scoreClass(score) + " " + EASINESS_SCORE,
      scoreStr(score)
    )
  }
}
