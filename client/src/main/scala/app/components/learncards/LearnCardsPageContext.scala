package app.components.learncards

import app.WsClient
import app.components.WindowFunc
import japgolly.scalajs.react.{Callback, CallbackTo}
import shared.api.CardsApi
import shared.dto.{Easiness, Score}
import shared.pageparams.LearnCardsPageParams

trait LearnCardsPageContext {

  //abstract members
  protected def modLearnCardsPageMem(f: LearnCardsPageMem => LearnCardsPageMem): CallbackTo[LearnCardsPageMem]
  val wsClient: WsClient[CardsApi]
  val learnCardsPageMem: LearnCardsPageMem
  val pageParams: LearnCardsPageParams
  protected def windowFunc: WindowFunc

  //actions

  def loadCardStates = wsClient.post(_.loadCardStates(pageParams.paragraphId), windowFunc.showError) {
    case topicStates => mod(_.copy(
      topic = None,
      topicStates = Some(topicStates),
      avgScore = topicStates
        .map(st => if (st.time.isDefined) st.score.level else 0)
        .map(s => s*100/3.0).sum / topicStates.size
    ))
  }

  def scoreSelected(topicId: Long)(easiness: Easiness, score: Score) = windowFunc.openWaitPane >>
    wsClient.post(_.updateCardState(topicId, easiness, score), windowFunc.showError) { _ =>
      loadCardStates >> windowFunc.closeWaitPane
    }

  def topicSelected(topicId: Long) = windowFunc.openWaitPane >>
    wsClient.post(_.loadTopic(topicId), windowFunc.showError) {
      topic => mod(_.copy(topic = Some(topic), topicStates = None)) >> windowFunc.closeWaitPane
    }

  //inner methods
  private def mod(f: LearnCardsPageMem => LearnCardsPageMem): Callback = modLearnCardsPageMem(f).void
  private def action(f: LearnCardsPageMem => Callback): Callback = modLearnCardsPageMem(m => m) >>= f
}