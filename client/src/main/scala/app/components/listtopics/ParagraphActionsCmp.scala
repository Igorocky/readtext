package app.components.listtopics

import app.Utils._
import app.components.WindowFunc
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.dto.Paragraph

object ParagraphActionsCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext,
                   paragraph: Paragraph,
                   onEdit: Callback,
                   onCreateTopic: Callback) {
    @inline def render = comp(this)
  }

  protected case class State(hidden: Boolean = true)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(implicit p: Props, s: State) = <.span(
      if (s.hidden) {
        showAllActionsButton
      } else {
        TagMod(
          hideAllActionsButton,
          editParagraphButton,
          createTopicButton,
          moveUpButton,
          moveDownButton,
          deleteParagraphButton
        )
      }
    )

    def showAllActionsButton(implicit p: Props) = buttonWithIcon(
      onClick = $.modState(_.copy(hidden = false)),
      btnType = BTN_INFO,
      iconType = "fa-arrow-right"
    )

    def hideAllActionsButton(implicit p: Props) = buttonWithIcon(
      onClick = $.modState(_.copy(hidden = true)),
      btnType = BTN_INFO,
      iconType = "fa-arrow-left"
    )

    def editParagraphButton(implicit p: Props) = buttonWithIcon(
      onClick = p.onEdit,
      btnType = BTN_INFO,
      iconType = "fa-pencil-square-o"
    )

    def createTopicButton(implicit p: Props) = buttonWithText(
      onClick = p.onCreateTopic,
      btnType = BTN_INFO,
      text = "Create topic"
    )

    def moveUpButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.moveUpParagraphAction(p.paragraph.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-up"
    )

    def moveDownButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.moveDownParagraphAction(p.paragraph.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-down"
    )

    def deleteParagraphButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.openOkCancelDialog(
        text = s"Delete paragraph '${p.paragraph.name}'?",
        onOk = p.ctx.openWaitPane >> p.ctx.wsClient.post(
          _.deleteParagraph(p.paragraph.id.get),
          th => p.ctx.openOkDialog("Could not delete paragraph: " + th.getMessage)
        ) { case () => p.ctx.paragraphDeleted(p.paragraph.id.get) >> p.ctx.closeWaitPane }
      ),
      btnType = BTN_DANGER,
      iconType = "fa-trash-o"
    )


  }
}