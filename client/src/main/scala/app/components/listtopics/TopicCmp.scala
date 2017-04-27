package app.components.listtopics

import app.Utils._
import app.components.Checkbox
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.SharedConstants.HIGHLIGHT_CHILD_SPAN_ON_HOVER
import shared.dto.{Topic, TopicUpdate}
import shared.forms.Forms
import upickle.default._

import scala.util.{Failure, Success}

object TopicCmp {

  protected case class Props(globalScope: GlobalScope,
                             topic: Topic)

  protected case class State(editMode: Boolean = false, showImg: Boolean = false)


  def apply(globalScope: GlobalScope, topic: Topic) =
    comp.withKey(topic.id.get.toString)(Props(globalScope, topic))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
      .initialState(State())
    .renderPS{ ($,props,state) =>
      if (!state.editMode) {
        <.div(^.`class` := this.getClass.getSimpleName + (if (props.topic.checked) " checked" else ""),
          <.div(^.`class` := HIGHLIGHT_CHILD_SPAN_ON_HOVER,
            checkboxForTopic(props.topic, props),
            showImgButton(props.topic, state, $.modState(_.copy(showImg = !state.showImg))),
            <.span(props.topic.title),
            editTopicButton(props.topic, props, $.modState(_.copy(editMode = true))),
            moveUpButton(props.topic, props),
            moveDownButton(props.topic, props),
            deleteTopicButton(props.topic, props)
          ),
          if (state.showImg) {
            <.div(props.topic.images.toVdomArray { img =>
              <.div(<.img(^.src := props.globalScope.pageParams.getTopicImgUrl + "/" + props.topic.id.get + "/" + img))
            })
          } else EmptyVdom
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
      onChange = newVal => props.globalScope.checkTopicsAction(List((topic.id.get, newVal))),
      checked = topic.checked
    )

  def moveUpButton(topic: Topic, props: Props) = buttonWithIcon(
    onClick = props.globalScope.moveUpTopicAction(topic.id.get),
    btnType = BTN_INFO,
    iconType = "fa-long-arrow-up"
  )

  def moveDownButton(topic: Topic, props: Props) = buttonWithIcon(
    onClick = props.globalScope.moveDownTopicAction(topic.id.get),
    btnType = BTN_INFO,
    iconType = "fa-long-arrow-down"
  )

  def editTopicButton(topic: Topic, props: Props, onClick: Callback) = buttonWithIcon(
    onClick = onClick,
    btnType = BTN_INFO,
    iconType = "fa-pencil-square-o"
  )

  def deleteTopicButton(topic: Topic, props: Props) = buttonWithIcon(
    onClick = props.globalScope.openOkDialog1(
      s"Delete topic '${topic.title}'?",
      post(url = props.globalScope.pageParams.deleteTopicUrl, data = topic.id.get.toString) {
        case Success(_) => props.globalScope.closeWaitPane >> props.globalScope.topicDeleted(topic.id.get)
        case Failure(th) => props.globalScope.openOkDialog("Could not delete topic: " + th.getMessage)
      }.void
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