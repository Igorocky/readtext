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
    .initialStateFromProps(p => ListTopicsPageState(pageParams = p))
    .renderBackend[Backend]
    .componentWillMount { $ =>
      dom.window.addEventListener[ClipboardEvent](
        "paste",
        (e: ClipboardEvent) => $.state.runPasteListener(JsGlobalScope.extractFileFromEvent(e))
      )
      $.modState {_.copy(
          modState = $.modState(_),
          wsClient = Utils.createWsClient($.props.wsEntryUrl),
          sessionWsClient = Utils.createWsClient($.props.wsEntryUrl)
      )}
    }
    .build

  protected class Backend($: BackendScope[Props, State]) {

    def render(implicit state: State) = UnivPage.Props(
      language = state.pageParams.headerParams.language,
      sessionWsClient = state.sessionWsClient,
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
          mapLazyTreeNode(state.listTopicsPageMem.data).render
        )
    ).render

    def mapLazyTreeNode(node: LazyTreeNode)(implicit ctx: WindowFunc with ListTopicsPageContext): Tree.Props =
      (node.value: @unchecked) match {
        case None => Tree.Props(
          key = "rootNode",
          nodeValue = None,
          mayHaveChildren = true,
          children = node.children.map(_.map(mapLazyTreeNode)),
          loadChildren = loadChildren(None),
          expanded = Some(true),
          onExpand = Callback.empty,
          onCollapse = Callback.empty
        )
        case Some(p: Paragraph) => Tree.Props(
          key = "par-" + p.id.get,
          nodeValue = Some(ParagraphCmp.Props(p, ctx, ctx.listTopicsPageMem.tagFilter).render),
          mayHaveChildren = true,
          children = node.children.map(_.map(mapLazyTreeNode)),
          loadChildren = loadChildren(p.id),
          expanded = Some(p.expanded),
          onExpand = ctx.expandParagraphsAction(List(p.id.get -> true)),
          onCollapse = ctx.expandParagraphsAction(List(p.id.get -> false))
        )
        case Some(t: Topic) => Tree.Props(
          key = "top-" + t.id.get,
          nodeValue = Some(TopicCmp.Props(ctx, t).render),
          mayHaveChildren = false,
          children = None,
          loadChildren = Callback.empty,
          expanded = None,
          onExpand = Callback.empty,
          onCollapse = Callback.empty
        )
      }

    def header(implicit state: State) =
      HeaderCmp.Props(
        ctx = state,
        /*paragraphs = props.paragraphs,*/
        filterChanged = state.filterChanged(_)
      ).render

    def loadChildren(paragraphId: Option[Long])(implicit ctx: ListTopicsPageContext with WindowFunc) = {
      def setChildren(children: List[Any]) =
        ctx.setChildren(paragraphId, children.map(c => LazyTreeNode(Some(c), None)))

      ctx.openWaitPane >> ctx.wsClient.post(_.loadParagraphsByParentId(paragraphId), _ => ctx.openOkDialog("Error loading paragraphs"))(
        paragraphs => if (paragraphId.isDefined) {
          ctx.wsClient.post(_.loadTopicsByParentId(paragraphId.get), _ => ctx.openOkDialog("Error loading topics"))(
            topics => setChildren(paragraphs ::: topics) >> ctx.closeWaitPane
          )
        } else {
          setChildren(paragraphs) >> ctx.closeWaitPane
        }
      )
    }
  }
}
