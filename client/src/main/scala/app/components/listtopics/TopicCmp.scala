package app.components.listtopics

import app.Utils._
import app.components.{Checkbox, WindowFunc}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.SharedConstants.{HIGHLIGHTED, HIGHLIGHT_CHILD_SPAN_ON_HOVER}
import shared.dto.Topic

object TopicCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext,
                   topic: Topic,
                   selected: Boolean) {
    @inline def render = comp.withKey("top-" + topic.id.get.toString)(this)
  }

  protected case class State(editMode: Boolean = false, showImg: Boolean = false)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderPS{ ($,props,state) =>
      if (!state.editMode) {
        <.div(^.`class` := this.getClass.getSimpleName /*+ (if (props.topic.checked) " checked" else "")*/,
          <.div(^.`class` := HIGHLIGHT_CHILD_SPAN_ON_HOVER,
            ^.onClick --> props.ctx.selectTopicAction(props.topic.id.get, !props.selected),
            if (props.ctx.listTopicsPageMem.selectMode) checkboxForTopic(props) else EmptyVdom,
            showImgButton(props.topic, state, $.modState(_.copy(showImg = !state.showImg))),
            <.span(
              ^.`class`:=HIGHLIGHTED,
              props.topic.title
            ),
            editTopicButton(props.topic, props, $.modState(_.copy(editMode = true))),
            moveUpButton(props.topic, props),
            moveDownButton(props.topic, props),
            deleteTopicButton(props.topic, props),
            TagsCmp.Props(
              ctx = props.ctx,
              submitFunction = tag => props.ctx.wsClient.post(
                _.addTagForTopic(tag),
                th => props.ctx.openOkDialog("Error adding tag: " + th.getMessage)
              ),
              entityId = props.topic.id.get,
              tags = props.topic.tags,
              tagAdded = props.ctx.tagAdded(props.topic.id.get, _),
              removeTag = props.ctx.removeTagAction(props.topic.id.get, _)
            ).render
          ),
          if (state.showImg) {
            <.div(props.topic.images.toVdomArray { img =>
              <.div(^.key:= img,
                <.img(^.src := props.ctx.pageParams.getTopicImgUrl + "/" + props.topic.id.get + "/" + img)
              )
            })
          } else EmptyVdom
        )
      } else {
        TopicForm.Props(
          submitFunction = topic => props.ctx.wsClient.post(
            _.updateTopic(topic),
            th => props.ctx.openOkDialog("Error updating topic: " + th.getMessage)
          ),
          topic = props.topic,
          cancelled = $.modState(_.copy(editMode = false)),
          submitComplete = topic => $.modState(_.copy(editMode = false)) >> props.ctx.topicUpdated(topic),
          textFieldLabel = "",
          submitButtonName = "Save",
          editMode = true,
          ctx = props.ctx
        ).render
      }

    }.build

  def checkboxForTopic(props: Props) = Checkbox.Props(
    checked = props.selected,
    onChange = newVal => props.ctx.selectTopicAction(props.topic.id.get, newVal)
  ).render

  def moveUpButton(topic: Topic, props: Props) = buttonWithIcon(
    onClick = props.ctx.moveUpTopicAction(topic.id.get),
    btnType = BTN_INFO,
    iconType = "fa-long-arrow-up"
  )

  def moveDownButton(topic: Topic, props: Props) = buttonWithIcon(
    onClick = props.ctx.moveDownTopicAction(topic.id.get),
    btnType = BTN_INFO,
    iconType = "fa-long-arrow-down"
  )

  def editTopicButton(topic: Topic, props: Props, onClick: Callback) = buttonWithIcon(
    onClick = onClick,
    btnType = BTN_INFO,
    iconType = "fa-pencil-square-o"
  )

  def deleteTopicButton(topic: Topic, props: Props) = buttonWithIcon(
    onClick = props.ctx.openOkCancelDialog(
      text = s"Delete topic '${topic.title}'?",
      onOk = props.ctx.openWaitPane >> props.ctx.wsClient.post(
        _.deleteTopic(topic.id.get),
        th => props.ctx.openOkDialog("Could not delete topic: " + th.getMessage)
      ) {
        case () => props.ctx.closeWaitPane >> props.ctx.topicDeleted(topic.id.get)
      }
    ),
    btnType = BTN_DANGER,
    iconType = "fa-trash-o"
  )

  def showImgButton(topic: Topic, state: State, onClick: Callback) = buttonWithIcon(
    onClick = onClick,
    btnType = if (topic.images.isEmpty) BTN_DEFAULT else BTN_INFO,
    iconType = "fa-bars",
    disabled = topic.images.isEmpty
  )
}