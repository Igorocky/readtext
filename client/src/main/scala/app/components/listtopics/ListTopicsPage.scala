package app.components.listtopics

import app.components._
import app.{JsGlobalScope, Utils}
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.raw.ClipboardEvent
import shared.SharedConstants._
import shared.dto.{Paragraph, Topic}
import shared.pageparams.ListTopicsPageParams
import upickle.default._

// TODO: components should not use WS clients
// TODO: add quick references (from a topic to another topic)
object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected type State = ListTopicsPageState

  def apply(str: String): Unmounted[Props, State, Backend] = comp(read[Props](str))

  // TODO: create preloading of expanded topics
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
          getState = () => $.state,
          wsClient = Utils.createWsClient($.props.wsEntryUrl),
          cardsClient = Utils.createWsClient($.props.wsEntryUrl),
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
          mapMainTopicTree(state.listTopicsPageMem.topicTree).render,
          if (state.listTopicsPageMem.selectParagraphTree.isDefined) {
            SelectParagraphDiagCmp.Props(ctx = state).render
          } else {
            EmptyVdom
          }
        )
    ).render

    def mapMainTopicTree(node: TopicTree)(implicit ctx: WindowFunc with ListTopicsPageContext): Tree.Props =
      (node.value: @unchecked) match {
        case None => Tree.Props(
          key = "rootNode",
          nodeValue = None,
          mayHaveChildren = true,
          children = node.children.map(_.map(mapMainTopicTree)),
          loadChildren = ctx.loadChildrenIntoMainTopicTree(None),
          expanded = Some(true),
          onExpand = Callback.empty,
          onCollapse = Callback.empty,
          outerClass = Some(MAIN_TOPIC_TREE)
        )
        case Some(p: Paragraph) => Tree.Props(
          key = "par-" + p.id.get,
          nodeValue = Some(ParagraphCmp.Props(
            paragraph = p,
            ctx = ctx,
            tagFilter = ctx.listTopicsPageMem.tagFilter,
            selected = node.attrs.selected,
            language = ctx.language,
            uploadTopicFileUrl = ctx.pageParams.uploadTopicFileUrl,
            getTopicImgUrl = ctx.pageParams.getTopicImgUrl,
            unregisterPasteListener = ctx.unregisterPasteListener,
            registerPasteListener = ctx.registerPasteListener
          ).render),
          mayHaveChildren = true,
          children = node.children.map(_.map(mapMainTopicTree)),
          loadChildren = ctx.loadChildrenIntoMainTopicTree(p.id),
          expanded = Some(p.expanded),
          onExpand = ctx.expandParagraphsAction(List(p.id.get -> true)),
          onCollapse = ctx.expandParagraphsAction(List(p.id.get -> false))
        )
        case Some(t: Topic) => Tree.Props(
          key = "top-" + t.id.get,
          nodeValue = Some(TopicCmp.Props(
            ctx = ctx,
            topic = t,
            selected = node.attrs.selected,
            showImg = node.attrs.showImg,
            actionsHidden = node.attrs.actionsHidden,
            selectMode = ctx.listTopicsPageMem.selectMode,
            getTopicImgUrl = ctx.pageParams.getTopicImgUrl,
            wsClient = ctx.wsClient,
            topicUpdated = ctx.topicUpdated,
            language = ctx.language,
            uploadTopicFileUrl = ctx.pageParams.uploadTopicFileUrl,
            unregisterPasteListener = ctx.unregisterPasteListener,
            registerPasteListener = ctx.registerPasteListener,
            readOnly = false
          ).render),
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


  }
}
