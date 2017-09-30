package app.components.listtopics

import app.Utils._
import app.WsClient
import app.components.{Checkbox, WindowFunc}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import org.scalajs.dom.raw.File
import shared.SharedConstants.{HIGHLIGHTED, HIGHLIGHT_CHILD_SPAN_ON_HOVER}
import shared.api.TopicApi
import shared.dto.Topic
import shared.messages.Language

trait TopicCmpActions {
  def changeTopicSelection(topicId: Long, selected: Boolean): Callback
  def showTopicImgBtnClicked(topicId: Long): Callback
}

// TODO: move all actions into per component trait
object TopicCmp {

  case class Props(ctx: WindowFunc with TopicCmpActions with ScoreCmpActions with TopicActionsCmpActions,
                   topic: Topic,
                   selected: Boolean,
                   showImg: Boolean,
                   actionsHidden: Boolean,
                   selectMode: Boolean,
                   getTopicImgUrl: String,
                   readOnly: Boolean,
                   language: Language,
                   uploadTopicFileUrl: String,
                   wsClient: WsClient[TopicApi],
                   topicUpdated: Topic => Callback,
                   unregisterPasteListener: Long => Callback,
                   registerPasteListener: (Long, File => Callback) => Callback
                  ) {
    @inline def render = comp.withKey("top-" + topic.id.get.toString)(this)
  }

  protected case class State(editMode: Boolean = false)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderPS{ ($,props,state) =>
      if (!state.editMode) {
        <.div(^.`class` := this.getClass.getSimpleName /*+ (if (props.topic.checked) " checked" else "")*/,
          <.div(^.`class` := HIGHLIGHT_CHILD_SPAN_ON_HOVER,
            ^.onClick --> props.ctx.changeTopicSelection(props.topic.id.get, !props.selected),
            if (props.selectMode) checkboxForTopic(props) else EmptyVdom,
            showImgButton(props.topic, state, props.ctx.showTopicImgBtnClicked(props.topic.id.get)),
            <.span(
              ^.`class`:=HIGHLIGHTED,
              props.topic.title
            ),
            TopicActionsCmp.Props(
              ctx = props.ctx,
              topic = props.topic,
              actionsHidden = props.actionsHidden,
              readOnly = props.readOnly,
              onEdit = $.modState(_.copy(editMode = true))
            ).render
          ),
          if (props.showImg) {
            TagMod(
              ScoreCmp.Props(
                ctx = props.ctx,
                cardId = props.topic.id.get,
                language = props.language
              ).render,
              <.div(props.topic.images.toVdomArray { img =>
                <.div(^.key:= img,
                  <.img(^.src := props.getTopicImgUrl + "/" + props.topic.id.get + "/" + img)
                )
              })
            )
          } else EmptyVdom
        )
      } else {
        TopicForm.Props(
          submitFunction = topic => props.wsClient.post(
            _.updateTopic(topic),
            th => props.ctx.openOkDialog("Error updating topic: " + th.getMessage)
          ),
          topic = props.topic,
          cancelled = $.modState(_.copy(editMode = false)),
          submitComplete = topic => $.modState(_.copy(editMode = false)) >> props.topicUpdated(topic),
          textFieldLabel = "",
          submitButtonName = "Save",
          editMode = true,
          ctx = props.ctx,
          language = props.language,
          uploadTopicFileUrl = props.uploadTopicFileUrl,
          getTopicImgUrl = props.getTopicImgUrl,
          unregisterPasteListener = props.unregisterPasteListener,
          registerPasteListener = props.registerPasteListener
        ).render
      }

    }.build

  def checkboxForTopic(props: Props) = Checkbox.Props(
    checked = props.selected,
    onChange = newVal => props.ctx.changeTopicSelection(props.topic.id.get, newVal)
  ).render

  def showImgButton(topic: Topic, state: State, onClick: Callback) = buttonWithIcon(
    onClick = onClick,
    btnType = if (topic.images.isEmpty) BTN_DEFAULT else BTN_INFO,
    iconType = "fa-bars",
    disabled = topic.images.isEmpty
  )
}