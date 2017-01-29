package app.components.listtopics

import app.components.Checkbox
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.Topic

object TopicCmp {
  type NewValueChecked = Boolean
  protected case class Props(
                              topic: Topic,
                              checkTopicAction: NewValueChecked => Callback
                            )

  def apply(topic: Topic, checkTopicAction: NewValueChecked => Callback) =
    comp(Props(topic, checkTopicAction))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { case props @ Props(topic, _) =>
      <.div(
        checkboxForTopic(topic, props),
        topic.title
      )
    }.build

  def checkboxForTopic(topic: Topic, props: Props) =
    Checkbox(
      id = "selectTopic-" + topic.id.get,
      onChange = props.checkTopicAction,
      checked = topic.checked
    )
}