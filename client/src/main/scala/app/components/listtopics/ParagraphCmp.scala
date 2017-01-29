package app.components.listtopics

import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.{Paragraph, Topic}
import shared.forms.Forms
import shared.messages.Language

object ParagraphCmp {
  type NewValueChecked = Boolean
  type NewValueExpanded = Boolean

  protected case class Props(
                              language: Language,
                              paragraph: Paragraph,
                              renameParagraphUrl: String,
                              checkParagraphAction: NewValueChecked => Callback,
                              expandParagraphAction: NewValueExpanded => Callback,
                              checkTopicAction: (Topic, NewValueChecked) => Callback,
                              paragraphRenamed: Paragraph => Callback
                            )

  protected case class State(
                              editMode: Boolean = false
                            )

  def apply(language: Language, paragraph: Paragraph, renameParagraphUrl: String,
            checkParagraphAction: NewValueChecked => Callback,
            expandParagraphAction: NewValueExpanded => Callback,
            checkTopicAction: (Topic, NewValueChecked) => Callback,
            paragraphRenamed: Paragraph => Callback) =
    comp(Props(language, paragraph, renameParagraphUrl,
      checkParagraphAction, expandParagraphAction, checkTopicAction, paragraphRenamed))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState(State())
    .renderPS{ ($, props, state) =>
        <.div(
          if (!state.editMode) {
            <.div(
              checkboxForParagraph(props.paragraph, props),
              expandParagraphButton(props.paragraph, props),
              props.paragraph.name,
              editParagraphButton(props, $.modState(_.copy(editMode = true)))
            )
          } else {
            ParagraphForm(
              language = props.language,
              formData = Forms.paragraphFrom.formData(props.paragraph).copy(submitUrl = props.renameParagraphUrl),
              cancelled = $.modState(_.copy(editMode = false)),
              submitComplete = par => $.modState(_.copy(editMode = false)) >> props.paragraphRenamed(par),
              textFieldTitle = "",
              submitButtonName = "Save"
            )
          },
          if (props.paragraph.expanded) listTopics(props.paragraph, props) else EmptyTag
        )
    }.build

  def editParagraphButton(props: Props, onClick: Callback) =
    Button(
      id = "edit-paragraph-btn-" + props.paragraph.id.get,
      name = "Rename",
      onClick = onClick
    )

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