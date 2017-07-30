package app.components.listtopics

import app.Utils._
import app.components.{ModalDialog, Tree, WindowFunc}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.SharedConstants.SELECTED_PARAGRAPH
import shared.dto.Paragraph

object SelectParagraphDiagCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext) {
    @inline def render = comp(this)
  }

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, Unit]) {
    def render(implicit p: Props) = {
      implicit val ctx = p.ctx
      val paragraphTree = p.ctx.listTopicsPageMem.selectParagraphTree.get
      ModalDialog(
        width = "500",
        content = <.div(
          <.div(
            mapSelectParagraphTree(paragraphTree).render
          ),
          <.div(
            buttonWithText(
              onClick = p.ctx.moveSelectedItems,
              btnType = BTN_INFO,
              text = "Move",
              disabled = paragraphTree.findNodes(_.selected).isEmpty
            ),
            buttonWithText(
              onClick = p.ctx.closeSelectParagraphWindow,
              btnType = BTN_INFO,
              text = "Cancel"
            )
          )
        )
      )
    }

    def mapSelectParagraphTree(node: TopicTree)(implicit ctx: WindowFunc with ListTopicsPageContext, props: Props): Tree.Props =
      (node.value: @unchecked) match {
        case None => Tree.Props(
          key = "rootNode",
          nodeValue = None,
          mayHaveChildren = true,
          children = node.children.map(
            _.filterNot{
              case TopicTree(Some(p:Paragraph), _, _) if props.ctx.listTopicsPageMem.selectedParagraphs.exists(_ == p.id.get) => true
              case _ => false
            }.map(mapSelectParagraphTree)
          ),
          loadChildren = ctx.loadChildrenIntoSelectParagraphTree(None),
          expanded = Some(true),
          onExpand = Callback.empty,
          onCollapse = Callback.empty
        )
        case Some(p: Paragraph) if p.id == None => Tree.Props(
          key = "rootNode",
          nodeValue = Some(drawParagraph(p.name, p.id, node.selected)),
          mayHaveChildren = true,
          children = node.children.map(
            _.filterNot{
              case TopicTree(Some(p:Paragraph), _, _) if props.ctx.listTopicsPageMem.selectedParagraphs.exists(_ == p.id.get) => true
              case _ => false
            }.map(mapSelectParagraphTree)
          ),
          loadChildren = ctx.loadChildrenIntoSelectParagraphTree(None),
          expanded = Some(true),
          onExpand = Callback.empty,
          onCollapse = Callback.empty
        )
        case Some(p: Paragraph) => Tree.Props(
          key = "par-" + p.id.get,
          nodeValue = Some(drawParagraph(p.name, p.id, node.selected)),
          mayHaveChildren = true,
          children = node.children.map(_.map(mapSelectParagraphTree)),
          loadChildren = ctx.loadChildrenIntoSelectParagraphTree(p.id),
          expanded = None,
          onExpand = Callback.empty,
          onCollapse = Callback.empty
        )
      }

    def drawParagraph(name: String, id: Option[Long], selected: Boolean)(implicit ctx: WindowFunc with ListTopicsPageContext, props: Props) =
      <.div(
        (^.`class`:=SELECTED_PARAGRAPH).when(selected),
        ^.onClick --> ctx.selectParagraphInDialogAction(id),
        name
      )
  }
}