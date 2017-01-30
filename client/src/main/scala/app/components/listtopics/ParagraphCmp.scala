package app.components.listtopics

import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.{Paragraph, Topic}
import shared.forms.{FormData, Forms}
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
                              paragraphRenamed: Paragraph => Callback,
                              createTopicUrl: String,
                              topicCreated: Topic => Callback,
                              updateTopicUrl: String,
                              topicUpdated: Topic => Callback
                            )

  protected case class State(
                              editMode: Boolean = false,
                              createTopicDiagOpened: Boolean = false
                            )

  def apply(language: Language, paragraph: Paragraph, renameParagraphUrl: String,
            checkParagraphAction: NewValueChecked => Callback,
            expandParagraphAction: NewValueExpanded => Callback,
            checkTopicAction: (Topic, NewValueChecked) => Callback,
            paragraphRenamed: Paragraph => Callback,
            createTopicUrl: String,
            topicCreated: Topic => Callback,
            updateTopicUrl: String,
            topicUpdated: Topic => Callback) =
    comp(Props(language, paragraph, renameParagraphUrl,
      checkParagraphAction, expandParagraphAction, checkTopicAction, paragraphRenamed,
      createTopicUrl, topicCreated,
      updateTopicUrl, topicUpdated))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState(State())
    .renderPS{ ($, props, state) =>
        <.div(
          if (!state.editMode) {
            <.div(
              checkboxForParagraph(props.paragraph, props),
              expandParagraphButton(props.paragraph, props),
              props.paragraph.name,
              editParagraphButton(props.paragraph, $.modState(_.copy(editMode = true))),
              createTopicButton(props.paragraph, $.modState(_.copy(createTopicDiagOpened = true))),
              if (state.createTopicDiagOpened) {
                createNewTopicDiag(props.paragraph, props, $.modState(_.copy(createTopicDiagOpened = false)))
              } else EmptyTag
            )
          } else {
            ParagraphForm(
              language = props.language,
              formData = Forms.paragraphForm.formData(props.paragraph).copy(submitUrl = props.renameParagraphUrl),
              cancelled = $.modState(_.copy(editMode = false)),
              submitComplete = par => $.modState(_.copy(editMode = false)) >> props.paragraphRenamed(par),
              textFieldTitle = "",
              submitButtonName = "Save"
            )
          },
          if (props.paragraph.expanded) listTopics(props.paragraph, props) else EmptyTag
        )
    }.build

  def editParagraphButton(paragraph: Paragraph, onClick: Callback) =
    Button(
      id = "edit-paragraph-btn-" + paragraph.id.get,
      name = "Rename",
      onClick = onClick
    )

  def createTopicButton(paragraph: Paragraph, onClick: Callback) =
    Button(
      id = "create-topic-btn-" + paragraph.id.get,
      name = "Create topic",
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
        language = props.language,
        topic = topic,
        checkTopicAction = props.checkTopicAction(topic, _),
        updateTopicUrl = props.updateTopicUrl,
        topicUpdated = props.topicUpdated
      )
    }

  def createNewTopicDiag(p: Paragraph, props: Props, closeDiag: Callback) =
    TopicForm(
      language = props.language,
      formData = Forms.topicForm.formData(Topic(paragraphId = p.id)).copy(submitUrl = props.createTopicUrl),
      cancelled = closeDiag,
      submitComplete = topic => closeDiag >> props.topicCreated(topic),
      textFieldTitle = "New topic",
      submitButtonName = "Create"
    )


}