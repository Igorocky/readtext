package app.components.listtopics

import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.Topic
import shared.forms.Forms

object TopicCmp {

  protected case class Props(globalScope: GlobalScope,
                             topic: Topic)

  protected case class State(editMode: Boolean = false)


  def apply(globalScope: GlobalScope, topic: Topic) =
    comp.withKey(topic.id.get)(Props(globalScope, topic))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
      .initialState(State())
    .renderPS{ ($,props,state) =>
      if (!state.editMode) {
        <.div(
          checkboxForTopic(props.topic, props),
          props.topic.title,
          editTopicButton(props.topic, props, $.modState(_.copy(editMode = true)))
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
          submitComplete = topic => $.modState(_.copy(editMode = false)) >> props.globalScope.topicUpdated(topic),
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
      onChange = props.globalScope.checkTopicAction(topic, _),
      checked = topic.checked
    )

  def editTopicButton(topic: Topic, props: Props, onClick: Callback) =
    Button(
      id = "edit-tioc-" + topic.id.get,
      name = "Edit",
      onClick = onClick
    )
}