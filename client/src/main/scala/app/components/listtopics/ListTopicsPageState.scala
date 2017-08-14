package app.components.listtopics

import app.WsClient
import app.components.{WindowFunc, WindowFuncMem}
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom.raw.File
import shared.api.{CardsApi, SessionApi, TopicApi}
import shared.messages.Language
import shared.pageparams.ListTopicsPageParams

case class ListTopicsPageState(modState: (ListTopicsPageState => ListTopicsPageState) => Callback = null,
                               getState: () => ListTopicsPageState = null,
                               windowFuncMem: WindowFuncMem = WindowFuncMem(),
                               listTopicsPageMem: ListTopicsPageMem = ListTopicsPageMem(),
                               wsClient: WsClient[TopicApi] = null,
                               cardsClient: WsClient[CardsApi] = null,
                               sessionWsClient: WsClient[SessionApi] = null,
                               pageParams: ListTopicsPageParams) extends WindowFunc with ListTopicsPageContext {

  override protected def modWindowFuncMem(f: WindowFuncMem => WindowFuncMem): Callback =
    modState(s => s.copy(windowFuncMem = f(s.windowFuncMem)))

  override protected def modListTopicsPageMem(f: (ListTopicsPageMem) => ListTopicsPageMem): CallbackTo[ListTopicsPageMem] =
    modState(s => s.copy(listTopicsPageMem = f(s.listTopicsPageMem))) >> CallbackTo(getState().listTopicsPageMem)

  override protected def windowFunc: WindowFunc = this

  def runPasteListener(file: File): Unit =
    if (listTopicsPageMem.pasteListeners.isEmpty) ()
    else listTopicsPageMem.pasteListeners.maxBy{case ((_, order), _) => order}._2(file).runNow()

  def setLanguage(language: Language): ListTopicsPageState =
    copy(
      pageParams = pageParams.copy(
        headerParams = pageParams.headerParams.copy(
          language = language
        )
      )
    )
}


