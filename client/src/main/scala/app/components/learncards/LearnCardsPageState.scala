package app.components.learncards

import app.components.{WindowFunc, WindowFuncMem}
import app.{Utils, WsClient}
import japgolly.scalajs.react.{Callback, CallbackTo}
import shared.api.{CardsApi, SessionApi, TopicApi}
import shared.messages.Language
import shared.pageparams.LearnCardsPageParams

case class LearnCardsPageState(modState: (LearnCardsPageState => LearnCardsPageState) => Callback = null,
                               getState: () => LearnCardsPageState = null,
                               windowFuncMem: WindowFuncMem = WindowFuncMem(),
                               learnCardsPageMem: LearnCardsPageMem = LearnCardsPageMem(),
                               wsClient: WsClient[CardsApi] = null,
                               topicApi: WsClient[TopicApi] = Utils.stubWsClient[TopicApi]("topicApiStub"),
                               sessionWsClient: WsClient[SessionApi] = null,
                               pageParams: LearnCardsPageParams) extends WindowFunc with LearnCardsPageContext {

  override protected def modWindowFuncMem(f: WindowFuncMem => WindowFuncMem): Callback =
    modState(s => s.copy(windowFuncMem = f(s.windowFuncMem)))



  override protected def modLearnCardsPageMem(f: (LearnCardsPageMem) => LearnCardsPageMem): CallbackTo[LearnCardsPageMem] =
    modState(s => s.copy(learnCardsPageMem = f(s.learnCardsPageMem))) >> CallbackTo(getState().learnCardsPageMem)

  override protected def windowFunc: WindowFunc = this

  def setLanguage(language: Language): LearnCardsPageState =
    copy(
      pageParams = pageParams.copy(
        headerParams = pageParams.headerParams.copy(
          language = language
        )
      )
    )
}


