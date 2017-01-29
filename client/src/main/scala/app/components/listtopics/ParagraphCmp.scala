package app.components.listtopics

import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.{Paragraph, Topic}

object ParagraphCmp {
  type NewValueChecked = Boolean
  type NewValueExpanded = Boolean

  protected case class Props(
                              paragraph: Paragraph,
                              checkParagraphAction: NewValueChecked => Callback,
                              expandParagraphAction: NewValueExpanded => Callback,
                              checkTopicAction: (Topic, NewValueChecked) => Callback
                            )

  def apply(paragraph: Paragraph,
            checkParagraphAction: NewValueChecked => Callback,
            expandParagraphAction: NewValueExpanded => Callback,
            checkTopicAction: (Topic, NewValueChecked) => Callback) =
    comp(Props(paragraph, checkParagraphAction, expandParagraphAction, checkTopicAction))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { case props @ Props(paragraph, _, _, _) =>
        <.div(
          <.div(
            checkboxForParagraph(paragraph, props),
            expandParagraphButton(paragraph, props),
            paragraph.name
          ),
          if (paragraph.expanded) listTopics(paragraph, props) else EmptyTag
        )
    }.build

  def checkboxForParagraph(paragraph: Paragraph, props: Props) =
    Checkbox(
      id = "selectPar-" + paragraph.id.get,
      onChange = props.checkParagraphAction,
      checked = paragraph.checked
    )

  def expandParagraphButton(paragraph: Paragraph, props: Props) =
    Button(
      id = "expandPar-" + paragraph.id.get,
      name = if (paragraph.expanded) "-" else "+",
      onClick = props.expandParagraphAction(!paragraph.expanded)
    )

  def listTopics(p: Paragraph, props: Props) =
    p.topics.map{topic=>
      TopicCmp(
        topic = topic,
        checkTopicAction = props.checkTopicAction(topic, _)
      )
    }




}