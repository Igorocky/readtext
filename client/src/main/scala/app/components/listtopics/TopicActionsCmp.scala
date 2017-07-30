package app.components.listtopics

import app.Utils._
import app.components.WindowFunc
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.dto.Topic

object TopicActionsCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext,
                   topic: Topic,
                   onEdit: Callback) {
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
          editTopicButton,
          moveUpButton,
          moveDownButton,
          deleteTopicButton
        )
      },
      TagsCmp.Props(
        readOnly = s.hidden,
        ctx = p.ctx,
        submitFunction = tag => p.ctx.wsClient.post(
          _.addTagForTopic(tag),
          th => p.ctx.openOkDialog("Error adding tag: " + th.getMessage)
        ),
        entityId = p.topic.id.get,
        tags = p.topic.tags,
        tagAdded = p.ctx.tagAdded(p.topic.id.get, _),
        removeTag = p.ctx.removeTagAction(p.topic.id.get, _)
      ).render
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

    def editTopicButton(implicit p: Props) = buttonWithIcon(
      onClick = p.onEdit,
      btnType = BTN_INFO,
      iconType = "fa-pencil-square-o"
    )

    def moveUpButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.moveUpTopicAction(p.topic.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-up"
    )

    def moveDownButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.moveDownTopicAction(p.topic.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-down"
    )

    def deleteTopicButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.openOkCancelDialog(
        text = s"Delete topic '${p.topic.title}'?",
        onOk = p.ctx.openWaitPane >> p.ctx.wsClient.post(
          _.deleteTopic(p.topic.id.get),
          th => p.ctx.openOkDialog("Could not delete topic: " + th.getMessage)
        ) {
          case () => p.ctx.closeWaitPane >> p.ctx.topicDeleted(p.topic.id.get)
        }
      ),
      btnType = BTN_DANGER,
      iconType = "fa-trash-o"
    )



  }
}