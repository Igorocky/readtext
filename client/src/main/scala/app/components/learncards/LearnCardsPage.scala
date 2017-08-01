package app.components.learncards

import app.Utils
import app.components._
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
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
        CallbackTo($.state) >>= (_.loadNextCardInfo)
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
      content = (if (s.learnCardsPageMem.card.isEmpty) {
        "Loading..."
      } else {
        LearnCardCmp.Props(
          key = s.learnCardsPageMem.card.get.cardId.toString,
          ctx = s,
          question = <.div(s.learnCardsPageMem.card.get.question),
          answer = <.div(s.learnCardsPageMem.card.get.answer),
          scoreSelected = s.scoreSelected(s.learnCardsPageMem.card.get.cardId)
        ).render
      })
    ).render

  }
}
