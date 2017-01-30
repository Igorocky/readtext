package app.components.listtopics

import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.Topic
import shared.forms.Forms
import shared.messages.Language

object TopicCmp {
  type NewValueChecked = Boolean
  protected case class Props(
                              language: Language,
                              topic: Topic,
                              checkTopicAction: NewValueChecked => Callback,
                              updateTopicUrl: String,
                              topicUpdated: Topic => Callback
                            )

  protected case class State(
                              editMode: Boolean = false
                            )


  def apply(language: Language, topic: Topic, checkTopicAction: NewValueChecked => Callback, updateTopicUrl: String,
            topicUpdated: Topic => Callback) =
    comp(Props(language, topic, checkTopicAction, updateTopicUrl, topicUpdated))

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
          language = props.language,
          formData = Forms.topicForm.formData(props.topic).copy(submitUrl = props.updateTopicUrl),
          cancelled = $.modState(_.copy(editMode = false)),
          submitComplete = topic => $.modState(_.copy(editMode = false)) >> props.topicUpdated(topic),
          textFieldTitle = "",
          submitButtonName = "Save"
        )
      }

    }.build

  def checkboxForTopic(topic: Topic, props: Props) =
    Checkbox(
      id = "selectTopic-" + topic.id.get,
      onChange = props.checkTopicAction,
      checked = topic.checked
    )

  def editTopicButton(topic: Topic, props: Props, onClick: Callback) =
    Button(
      id = "edit-tioc-" + topic.id.get,
      name = "Edit",
      onClick = onClick
    )
}