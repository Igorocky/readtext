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
            TopicActionsCmp.Props(
              ctx = props.ctx,
              topic = props.topic,
              onEdit = $.modState(_.copy(editMode = true))
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

  def showImgButton(topic: Topic, state: State, onClick: Callback) = buttonWithIcon(
    onClick = onClick,
    btnType = if (topic.images.isEmpty) BTN_DEFAULT else BTN_INFO,
    iconType = "fa-bars",
    disabled = topic.images.isEmpty
  )
}