package app.components.listtopics

import app.Utils
import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.SharedConstants.HIGHLIGHT_ON_HOVER
import shared.dto.{Topic, TopicUpdate}
import shared.forms.Forms
import upickle.default._

import scala.util.{Failure, Success}

object TopicCmp {

  protected case class Props(globalScope: GlobalScope,
                             topic: Topic)

  protected case class State(editMode: Boolean = false, showImg: Boolean = false)


  def apply(globalScope: GlobalScope, topic: Topic) =
    comp.withKey(topic.id.get)(Props(globalScope, topic))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
      .initialState(State())
    .renderPS{ ($,props,state) =>
      if (!state.editMode) {
        <.div(
          <.div(^.`class` := HIGHLIGHT_ON_HOVER,
            checkboxForTopic(props.topic, props),
            showImgButton(props.topic, state, $.modState(_.copy(showImg = !state.showImg))),
            props.topic.title,
            editTopicButton(props.topic, props, $.modState(_.copy(editMode = true))),
            moveUpButton(props.topic, props),
            moveDownButton(props.topic, props),
            deleteTopicButton(props.topic, props)
          ),
          if (state.showImg) {
            <.div(props.topic.images.map { img =>
              <.div(<.img(^.src := props.globalScope.pageParams.getTopicImgUrl + "/" + props.topic.id.get + "/" + img))
            })
          } else EmptyTag
        )
      } else {
        TopicForm(
          formData = Forms.topicForm.formData(
            props.globalScope.language,
            props.topic,
            props.globalScope.pageParams.updateTopicUrl
          ),
          topic = Some(props.topic),
          cancelled = $.modState(_.copy(editMode = false)),
          submitComplete = str =>
            $.modState(_.copy(editMode = false)) >>
              props.globalScope.topicUpdated(read[TopicUpdate](str)),
          textFieldLabel = "",
          submitButtonName = "Save",
          editMode = true,
          globalScope = props.globalScope
        )
      }

    }.build

  def checkboxForTopic(topic: Topic, props: Props) =
    Checkbox(
      id = "selectTopic-" + topic.id.get,
      onChange = props.globalScope.checkAction(topic.id.get, _),
      checked = topic.checked
    )

  def moveUpButton(topic: Topic, props: Props) =
    Button(
      id = "move-up-topic-" + topic.id.get,
      name = "Up",
      onClick = props.globalScope.moveUpAction(topic.id.get)
    )

  def moveDownButton(topic: Topic, props: Props) =
    Button(
      id = "move-down-topic-" + topic.id.get,
      name = "Down",
      onClick = props.globalScope.moveDownAction(topic.id.get)
    )

  def editTopicButton(topic: Topic, props: Props, onClick: Callback) =
    Button(
      id = "edit-topic-btn-" + topic.id.get,
      name = "Edit",
      onClick = onClick
    )

  def deleteTopicButton(topic: Topic, props: Props) =
    Button(
      id = "delete-topic-btn-" + topic.id.get,
      name = "Delete topic",
      onClick = props.globalScope.openOkCancelDialog1(
        s"Delete topic '${topic.title}'?",
        Utils.post(url = props.globalScope.pageParams.deleteTopicUrl, data = topic.id.get.toString) {
          case Success(_) => props.globalScope.closeWaitPane >> props.globalScope.topicDeleted(topic.id.get)
          case Failure(th) => props.globalScope.openOkDialog("Could not delete topic: " + th.getMessage)
        }.void
      )
    )

  def showImgButton(topic: Topic, state: State, onClick: Callback) =
    Button(
      id = "show-img-topic-btn-" + topic.id.get,
      name = if (!state.showImg) "Show image" else "Hide image",
      onClick = onClick,
      disabled = topic.images.isEmpty
    )
}