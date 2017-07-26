package app.components.listtopics

import app.components._
import app.{JsGlobalScope, LazyTreeNode, Utils}
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.raw.ClipboardEvent
import shared.dto.{Paragraph, Topic}
import shared.pageparams.ListTopicsPageParams
import upickle.default._

object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected type State = ListTopicsPageState

  def apply(str: String): Unmounted[Props, State, Backend] = comp(read[Props](str))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(_ => ListTopicsPageState())
    .renderBackend[Backend]
    .componentWillMount { $ =>
      dom.window.addEventListener[ClipboardEvent](
        "paste",
        (e: ClipboardEvent) => $.state.runPasteListener(JsGlobalScope.extractFileFromEvent(e))
      )
      $.modState{s =>
        val ss = s.copy(modState = $.modState(_))
        ss.setGlobalScope(ListTopicsPageGlobalScope(
          pageParams = $.props,
          registerPasteListener = (id, listener) => $.modState(_.registerListener(id, listener)),
          unregisterPasteListener = id => $.modState(s => s.copy(pasteListeners = s.pasteListeners.filterNot(_._1._2 == id))),
          wsClient = Utils.createWsClient($.props.wsEntryUrl),
          sessionWsClient = Utils.createWsClient($.props.wsEntryUrl),
          expandParagraphsAction = ids => $.backend.wsClient.post(_.expand(ids), ss.showError) {
            case () => $.modState(_.expandParagraphs(ids))
          },
          moveUpParagraphAction = id => $.backend.wsClient.post(_.moveUpParagraph(id), ss.showError) {
            case () => $.modState(_.moveUpParagraph(id))
          },
          moveUpTopicAction = id => $.backend.wsClient.post(_.moveUpTopic(id), ss.showError) {
            case () => $.modState(_.moveUpTopic(id))
          },
          moveDownParagraphAction = id => $.backend.wsClient.post(_.moveDownParagraph(id), ss.showError) {
            case () => $.modState(_.moveDownParagraph(id))
          },
          moveDownTopicAction = id => $.backend.wsClient.post(_.moveDownTopic(id), ss.showError) {
            case () => $.modState(_.moveDownTopic(id))
          },
          tagAdded = (topicId, newTags) => $.modState(_.setTags(topicId, newTags)),
          removeTagAction = (topicId, tag) => $.backend.wsClient.post(_.removeTagFromTopic(topicId, tag), ss.showError) {
            case tags => $.modState(_.setTags(topicId, tags))
          },
          paragraphCreated = p => $.modState(_.addParagraph(p)),
          paragraphUpdated = par => $.modState(_.updateParagraph(par)),
          paragraphDeleted = par => $.modState(_.deleteParagraph(par)) >> ss.closeWaitPane,
          topicCreated = topic => $.modState(_.addTopic(topic)),
          topicUpdated = top => $.modState(_.updateTopic(top)),
          topicDeleted = topId => $.modState(_.deleteTopic(topId)),
          filterTopic = (str, topic) => {
            val strUpper = str.trim.toUpperCase
            topic.tags.exists(tag => tag.toUpperCase.contains(strUpper))
          }
        ))}
    }
    .build

  protected class Backend($: BackendScope[Props, State]) {
    lazy val wsClient = $.state.runNow().globalScope.wsClient

    def render(implicit props: Props, state: State) = UnivPage.Props(
      language = state.globalScope.pageParams.headerParams.language,
      sessionWsClient = state.globalScope.sessionWsClient,
      onLanguageChange = newLang => $.modState(_.setLanguage(newLang)),
      windowFuncMem = state.windowFuncMem,
      windowFunc = state,
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
          Tree(mapLazyTreeNode(state.data))
        )
    ).render

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
          nodeValue = Some(ParagraphCmp.Props(p, state, state.globalScope, state.tagFilter).render),
          mayHaveChildren = true,
          getChildren = node.children.map(_.map(mapLazyTreeNode)),
          loadChildren = loadChildren(p.id),
          expanded = Some(p.expanded),
          onExpand = state.globalScope.expandParagraphsAction(List((p.id.get, true))),
          onCollapse = state.globalScope.expandParagraphsAction(List((p.id.get, false)))
        )
        case Some(t: Topic) => TreeNodeModel(
          key = "top-" + t.id.get,
          nodeValue = Some(TopicCmp.Props(state, state.globalScope, t).render),
          mayHaveChildren = false,
          getChildren = None,
          loadChildren = Callback.empty
        )
      }

    def header(implicit props: Props, state: State) =
      HeaderCmp.Props(
        windowFunc = state,
        globalScope = state.globalScope,
        /*paragraphs = props.paragraphs,*/
        filterChanged = str => $.modState(_.copy(tagFilter = str))
      ).render

    def loadChildren(paragraphId: Option[Long])(implicit props: Props, state: State) = {
      def setChildren(children: List[Any]) =
        $.modState(_.setChildren(paragraphId, children.map(c => LazyTreeNode(Some(c), None))))

      state.openWaitPane >> wsClient.post(_.loadParagraphsByParentId(paragraphId), _ => state.openOkDialog("Error loading paragraphs"))(
        paragraphs => if (paragraphId.isDefined) {
          wsClient.post(_.loadTopicsByParentId(paragraphId.get), _ => state.openOkDialog("Error loading topics"))(
            topics => setChildren(paragraphs ::: topics) >> state.closeWaitPane
          )
        } else {
          setChildren(paragraphs) >> state.closeWaitPane
        }
      )
    }
  }
}
