package app.components.listtopics

import app.components._
import app.{JsGlobalScope, Utils}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactExt_DomNodeO, Ref}
import org.scalajs.dom
import org.scalajs.dom.raw.{ClipboardEvent, HTMLInputElement}
import shared.forms.{DataResponse, ErrorResponse}
import shared.pageparams.ListTopicsPageParams
import upickle.default._

import scala.util.{Failure, Success}

object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected type State = ListTopicsState

  def apply(str: String) = comp(read[Props](str))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(_ => ListTopicsState())
    .renderBackend[Backend]
    .componentWillMount { $ =>
      dom.window.addEventListener[ClipboardEvent](
        "paste",
        (e: ClipboardEvent) => $.state.runPasteListener(JsGlobalScope.extractFileFromEvent(e))
      )
      $.modState(_.setGlobalScope(GlobalScope(
        pageParams = $.props,
        openOkDialog = str => $.backend.openOkDialog(str),
        openOkCancelDialog = (text, onOk, onCancel) => $.backend.openOkCancelDialog(text, onOk, onCancel),
        openOkCancelDialog1 = (text, onOk) => $.backend.openOkCancelDialog(text, onOk),
        openWaitPane = $.backend.openWaitPane,
        closeWaitPane = $.backend.closeWaitPane,
        registerPasteListener = (id, listener) => $.modState(_.registerListener(id,listener)),
        unregisterPasteListener = id => $.modState(s => s.copy(pasteListeners = s.pasteListeners.filterNot(_._1._2 == id))),
        expandParagraphAction = (parId, newExpanded) => $.backend.doAction(
          $.props.expandUrl,
          write((parId, newExpanded)),
          _ => $.modState(_.expandParagraph(parId, newExpanded))
        ),
        checkAction = (id, newChecked) => $.backend.doAction(
          $.props.checkUrl,
          write((id, newChecked)),
          _ => $.modState(_.checkById(id, newChecked))
        ),
        moveUpAction = id => $.backend.doAction(
          $.props.moveUpUrl,
          id.toString,
          _ => $.modState(_.moveUpById(id))
        ),
        moveDownAction = id => $.backend.doAction(
          $.props.moveDownUrl,
          id.toString,
          _ => $.modState(_.moveDownById(id))
        ),
        paragraphCreated = p => $.modState(_.addParagraph(p)),
        paragraphUpdated = parUpd => $.modState(_.updateParagraph(parUpd)),
        paragraphDeleted = par => $.modState(_.deleteParagraph(par)) >> $.backend.closeWaitPane,
        topicCreated = topic => $.modState(_.addTopic(topic)),
        topicUpdated = topUpd => $.modState(_.updateTopic(topUpd)),
        topicDeleted = topId => $.modState(_.deleteTopic(topId))
      )))
    }
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = UnivPage(
      language = state.globalScope.pageParams.headerParams.language,
      changeLangUrl = props.headerParams.changeLanguageUrl,
      onLanguageChange = newLang => $.modState(_.setLanguage(newLang)),
      content =
        <.div(
          header(state, props),
          state.paragraphs.map{paragraph =>
            ParagraphCmp(paragraph, state.globalScope)
          },
          waitPaneIfNecessary(state),
          okDialogIfNecessary(state),
          okCancelDialogIfNecessary(state)
        )
    )

    def header(state: State, props: Props) = HeaderCmp(state.globalScope)

    def waitPaneIfNecessary(state: State): TagMod =
      if (state.waitPane) {
        if (state.okDiagText.isDefined || state.okCancelDiagText.isDefined) WaitPane() else WaitPane("rgba(255,255,255,0.0)")
      } else EmptyTag

    def okDialogIfNecessary(state: State): TagMod =
      if (state.okDiagText.isDefined) {
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(state.okDiagText.get),
            <.div(Button(id = "ok-diag-ok-btn", name = "OK", onClick = closeOkDialog))
          )
        )
      } else {
        EmptyTag
      }

    def okCancelDialogIfNecessary(state: State): TagMod =
      if (state.okCancelDiagText.isDefined) {
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(state.okCancelDiagText.get),
            <.div(
              Button(id = "ok-cancel-diag-ok-btn", name = "OK", onClick = closeOkCancelDialog >> state.onOk),
              Button(id = "ok-cancel-diag-cancel-btn", name = "Cancel", onClick = closeOkCancelDialog >> state.onCancel)
            )
          )
        )
      } else {
        EmptyTag
      }

    def openWaitPane: Callback = $.modState(_.copy(waitPane = true))
    def closeWaitPane: Callback = $.modState(_.copy(waitPane = false))
    def openOkDialog(text: String): Callback = openWaitPane >> $.modState(_.copy(okDiagText = Some(text)))
    def closeOkDialog: Callback = $.modState(_.copy(okDiagText = None)) >> closeWaitPane
    def openOkCancelDialog(text: String, onOk: Callback, onCancel: Callback): Callback =
      openWaitPane >> $.modState(_.copy(okCancelDiagText = Some(text), onOk = onOk, onCancel = onCancel))
    def openOkCancelDialog(text: String, onOk: Callback): Callback =
      openWaitPane >> $.modState(_.copy(okCancelDiagText = Some(text), onOk = onOk, onCancel = Callback.empty))
    def closeOkCancelDialog: Callback =
      $.modState(_.copy(okCancelDiagText = None, onOk = Callback.empty, onCancel = Callback.empty)) >> closeWaitPane

    def doAction(doActionUrl: String, data: String, onSuccess: String => Callback): Callback =
      openWaitPane >> Utils.post(url = doActionUrl, data = data){
        case Success(DataResponse(str)) => onSuccess(str) >> closeWaitPane
        case Success(ErrorResponse(str)) =>
          println("ERROR - " + str)
          openOkDialog(str)
        case Failure(throwable) =>
          println("ERROR - " + throwable.getMessage)
          openOkDialog(throwable.getMessage)
        case _ => ???
      }.void
  }
}
