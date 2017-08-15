package app.components.listtopics

import app.Utils._
import app.WsClient
import app.components.{Checkbox, WindowFunc}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import org.scalajs.dom.raw.File
import shared.SharedConstants.{HIGHLIGHTED, HIGHLIGHT_CHILD_SPAN_ON_HOVER}
import shared.api.{CardsApi, TopicApi}
import shared.dto.Topic
import shared.messages.Language

object TopicCmp {

  case class Props(ctx: WindowFunc,
                   topic: Topic,
                   selected: Boolean,
                   showImg: Boolean,
                   actionsHidden: Boolean,
                   selectTopicAction: (Long, Boolean) => Callback,
                   selectMode: Boolean,
                   showTopicImgBtnClicked: Callback,
                   getTopicImgUrl: String,
                   wsClient: WsClient[TopicApi],
                   topicUpdated: Topic => Callback,
                   showTopicActions: (Long, Boolean) => Callback,
                   cardsClient: WsClient[CardsApi],
                   moveUpTopicAction: Long => Callback,
                   moveDownTopicAction: Long => Callback,
                   topicDeleted: Long => Callback,
                   language: Language,
                   uploadTopicFileUrl: String,
                   unregisterPasteListener: Long => Callback,
                   registerPasteListener: (Long, File => Callback) => Callback,
                   topicStateUpdated: Long => Callback,
                   readOnly: Boolean
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
            ^.onClick --> props.selectTopicAction(props.topic.id.get, !props.selected),
            if (props.selectMode) checkboxForTopic(props) else EmptyVdom,
            showImgButton(props.topic, state, props.showTopicImgBtnClicked),
            <.span(
              ^.`class`:=HIGHLIGHTED,
              props.topic.title
            ),
            TopicActionsCmp.Props(
              ctx = props.ctx,
              topic = props.topic,
              onEdit = $.modState(_.copy(editMode = true)),
              actionsHidden = props.actionsHidden,
              showTopicActions = props.showTopicActions,
              cardsClient = props.cardsClient,
              moveUpTopicAction = props.moveUpTopicAction,
              moveDownTopicAction = props.moveDownTopicAction,
              topicDeleted = props.topicDeleted,
              wsClient = props.wsClient,
              readOnly = props.readOnly
            ).render
          ),
          if (props.showImg) {
            TagMod(
              ScoreCmp.Props(
                ctx = props.ctx,
                entityId = props.topic.id.get,
                cardsClient = props.cardsClient,
                language = props.language,
                topicStateUpdated = props.topicStateUpdated
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
    onChange = newVal => props.selectTopicAction(props.topic.id.get, newVal)
  ).render

  def showImgButton(topic: Topic, state: State, onClick: Callback) = buttonWithIcon(
    onClick = onClick,
    btnType = if (topic.images.isEmpty) BTN_DEFAULT else BTN_INFO,
    iconType = "fa-bars",
    disabled = topic.images.isEmpty
  )
}