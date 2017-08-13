package app.components.learncards

import app.WsClient
import app.components.WindowFunc
import japgolly.scalajs.react.{Callback, CallbackTo}
import shared.api.CardsApi
import shared.pageparams.LearnCardsPageParams

trait LearnCardsPageContext {

  //abstract members
  protected def modLearnCardsPageMem(f: LearnCardsPageMem => LearnCardsPageMem): CallbackTo[LearnCardsPageMem]
  val wsClient: WsClient[CardsApi]
  val learnCardsPageMem: LearnCardsPageMem
  val pageParams: LearnCardsPageParams
  protected def windowFunc: WindowFunc

  //actions

  def loadCardStates = Callback.empty/*wsClient.post(_.loadCardStates(pageParams.paragraphId), windowFunc.showError) {
    case topicStates => mod(_.copy(
      topic = None,
      topicStates = Some(topicStates)
    ))
  }
*/
  def scoreSelected(topicId: Long)(score: String) = windowFunc.openWaitPane >>
    wsClient.post(_.updateCardState(topicId, score), windowFunc.showError) { _ =>
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