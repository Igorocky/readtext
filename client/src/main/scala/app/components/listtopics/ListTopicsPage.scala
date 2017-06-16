package app.components.listtopics

import app.Utils._
import app.components._
import app.{JsGlobalScope, LazyTreeNode, Utils}
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.raw.ClipboardEvent
import shared.api.TopicApi
import shared.dto.{Paragraph, Topic}
import shared.forms.{DataResponse, ErrorResponse}
import shared.pageparams.ListTopicsPageParams
import upickle.default._

import scala.util.{Failure, Success}

object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected type State = ListTopicsState

  def apply(str: String): Unmounted[Props, State, Backend] = comp(read[Props](str))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(_ => ListTopicsState())
    .renderBackend[Backend]
    .componentWillMount { $ =>
      dom.window.addEventListener[ClipboardEvent](
        "paste",
        (e: ClipboardEvent) => $.state.runPasteListener(JsGlobalScope.extractFileFromEvent(e))
      )
      $.modState(_.setGlobalScope(ListTopicsPageGlobalScope(
        pageParams = $.props,
        openOkDialog = str => $.backend.openOkDialog(str),
        openOkCancelDialog = (text, onOk, onCancel) => $.backend.openOkCancelDialog(text, onOk, onCancel),
        openOkDialog1 = (text, onOk) => $.backend.openOkCancelDialog(text, onOk),
        openWaitPane = $.backend.openWaitPane,
        closeWaitPane = $.backend.closeWaitPane,
        registerPasteListener = (id, listener) => $.modState(_.registerListener(id,listener)),
        unregisterPasteListener = id => $.modState(s => s.copy(pasteListeners = s.pasteListeners.filterNot(_._1._2 == id))),
        wsClient = Utils.createWsClient($.props.wsEntryUrl),
        expandParagraphsAction = ids => $.backend.doAction(
          $.props.expandUrl,
          write(ids),
          _ => $.modState(_.expandParagraphs(ids))
        ),
        moveUpParagraphAction = id => $.backend.doAction(
          $.props.moveUpParagraphUrl,
          id.toString,
          _ => $.modState(_.moveUpParagraph(id))
        ),
        moveUpTopicAction = id => $.backend.doAction(
          $.props.moveUpTopicUrl,
          id.toString,
          _ => $.modState(_.moveUpTopic(id))
        ),
        moveDownParagraphAction = id => $.backend.doAction(
          $.props.moveDownParagraphUrl,
          id.toString,
          _ => $.modState(_.moveDownParagraph(id))
        ),
        moveDownTopicAction = id => $.backend.doAction(
          $.props.moveDownTopicUrl,
          id.toString,
          _ => $.modState(_.moveDownTopic(id))
        ),
        tagAdded = (topicId, newTags) => $.modState(_.setTags(topicId, newTags)),
        removeTagAction = (topicId, tag) => $.backend.doAction(
          $.props.removeTagFromTopicUrl,
          write((topicId, tag)),
          ans => $.modState(_.setTags(topicId, read[List[String]](ans)))
        ),
        paragraphCreated = p => $.modState(_.addParagraph(p)),
        paragraphUpdated = parUpd => $.modState(_.updateParagraph(parUpd)),
        paragraphDeleted = par => $.modState(_.deleteParagraph(par)) >> $.backend.closeWaitPane,
        topicCreated = topic => $.modState(_.addTopic(topic)),
        topicUpdated = topUpd => $.modState(_.updateTopic(topUpd)),
        topicDeleted = topId => $.modState(_.deleteTopic(topId)),
        filterTopic = (str, topic) => {
          val strUpper = str.trim.toUpperCase
          topic.tags.exists(tag => tag.toUpperCase.contains(strUpper))
        }
      )))
    }
    .build

  protected class Backend($: BackendScope[Props, State]) {
    private lazy val wsClient = $.state.runNow().globalScope.wsClient

    def render(implicit props: Props, state: State) = UnivPage(
      language = state.globalScope.pageParams.headerParams.language,
      changeLangUrl = props.headerParams.changeLanguageUrl,
      onLanguageChange = newLang => $.modState(_.setLanguage(newLang)),
      content =
        <.div(
          header,
          /*(if (state.tagFilter.trim.isEmpty) {
            state.paragraphs
          } else {
            state.paragraphs.filter(par =>
              par.topics.exists(top =>
                state.globalScope.filterTopic(state.tagFilter, top)
              )
            )
          }).toVdomArray{paragraph =>
            ParagraphCmp(paragraph, state.globalScope, state.tagFilter)
          },*/
          Tree(mapLazyTreeNode(state.data)),
          waitPaneIfNecessary(state),
          okDialogIfNecessary(state),
          okCancelDialogIfNecessary(state)
        )
    )

    def mapLazyTreeNode(node: LazyTreeNode)(implicit props: Props, state: State): TreeNodeModel =
      (node.value: @unchecked) match {
        case None => TreeNodeModel(
          key = "rootNode",
          nodeValue = None,
          mayHaveChildren = true,
          getChildren = node.children.map(_.map(mapLazyTreeNode)),
          loadChildren = loadChildren(None),
          expanded = Some(true),
          onExpand = Callback.empty,
          onCollapse = Callback.empty
        )
        case Some(p: Paragraph) => TreeNodeModel(
          key = "par-" + p.id.get,
          nodeValue = Some(ParagraphCmp(p, state.globalScope, state.tagFilter)),
          mayHaveChildren = true,
          getChildren = node.children.map(_.map(mapLazyTreeNode)),
          loadChildren = loadChildren(p.id),
          expanded = Some(p.expanded),
          onExpand = $.modState(_.expandParagraph(p.id.get, true)),
          onCollapse = $.modState(_.expandParagraph(p.id.get, false))
        )
        case Some(t: Topic) => TreeNodeModel(
          key = "top-" + t.id.get,
          nodeValue = Some(TopicCmp(state.globalScope, t)),
          mayHaveChildren = false,
          getChildren = None,
          loadChildren = Callback.empty
        )
      }

    def header(implicit props: Props, state: State) =
      HeaderCmp(
        globalScope = state.globalScope,
        /*paragraphs = props.paragraphs,*/
        filterChanged = str => $.modState(_.copy(tagFilter = str))
      )

    def waitPaneIfNecessary(state: State): TagMod =
      if (state.waitPane) {
        if (state.okDiagText.isDefined || state.okCancelDiagText.isDefined) WaitPane() else WaitPane("rgba(255,255,255,0.0)")
      } else EmptyVdom

    def okDialogIfNecessary(state: State): TagMod =
      state.okDiagText.whenDefined(text=>
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(state.okDiagText.get),
            buttonWithText(
              onClick = closeOkDialog,
              btnType = BTN_PRIMARY,
              text = "OK"
            )
          )
        )
      )

    def okCancelDialogIfNecessary(state: State): TagMod =
      state.okCancelDiagText.whenDefined(text =>
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(text),
            <.div(
              buttonWithText(
                onClick = closeOkCancelDialog >> state.onOk,
                btnType = BTN_PRIMARY,
                text = "OK"
              ),
              buttonWithText(
                onClick = closeOkCancelDialog >> state.onCancel,
                btnType = BTN_DEFAULT,
                text = "Cancel"
              )
            )
          )
        )
      )

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

    def loadChildren(paragraphId: Option[Long])(implicit props: Props) = {
      def setChildren(children: List[Any]) =
        $.modState(_.setChildren(paragraphId, children.map(c => LazyTreeNode(Some(c), None))))

      openWaitPane >> wsClient.post(_.loadParagraphsByParentId(paragraphId), _ => openOkDialog("Error loading paragraphs"))(
        paragraphs => if (paragraphId.isDefined) {
          wsClient.post(_.loadTopicsByParentId(paragraphId.get), _ => openOkDialog("Error loading topics"))(
            topics => setChildren(paragraphs ::: topics) >> closeWaitPane
          )
        } else {
          setChildren(paragraphs) >> closeWaitPane
        }
      )
    }
  }
}
