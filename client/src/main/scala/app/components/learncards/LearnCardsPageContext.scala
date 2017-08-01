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

  def loadNextCardInfo = wsClient.post(_.loadNextCardInfo(pageParams.poolId), windowFunc.showError) {
    case cardInfo => mod(_.copy(card = Some(cardInfo)))
  }

  def scoreSelected(cardId: Long)(easiness: Int, score: Int) = windowFunc.openWaitPane >>
    wsClient.post(_.scoreSelected(cardId, easiness, score), windowFunc.showError) {_ =>
      loadNextCardInfo >> windowFunc.closeWaitPane
    }

  //inner methods
  private def mod(f: LearnCardsPageMem => LearnCardsPageMem): Callback = modLearnCardsPageMem(f).void
  private def action(f: LearnCardsPageMem => Callback): Callback = modLearnCardsPageMem(m => m) >>= f
}