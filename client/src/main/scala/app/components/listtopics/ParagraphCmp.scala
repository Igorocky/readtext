package app.components.listtopics

import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.{Paragraph, Topic}
import shared.forms.Forms

object ParagraphCmp {
  protected case class Props(paragraph: Paragraph,
                             globalScope: GlobalScope)

  protected case class State(editMode: Boolean = false,
                             createTopicDiagOpened: Boolean = false)

  def apply(paragraph: Paragraph, globalScope: GlobalScope) = comp.withKey(paragraph.id.get)(Props(paragraph, globalScope))

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
              globalScope = props.globalScope,
              formData = Forms.paragraphForm.formData(
                props.globalScope.pageParams.headerParams.language, props.paragraph,
                props.globalScope.pageParams.renameParagraphUrl
              ),
              cancelled = $.modState(_.copy(editMode = false)),
              submitComplete = par => $.modState(_.copy(editMode = false)) >> props.globalScope.paragraphRenamed(par),
              textFieldLabel = "",
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
      onChange = props.globalScope.checkParagraphAction(paragraph, _),
      checked = paragraph.checked
    )

  def expandParagraphButton(paragraph: Paragraph, props: Props) =
    Button(
      id = "expandPar-" + paragraph.id.get,
      name = if (paragraph.expanded) "-" else "+",
      onClick = props.globalScope.expandParagraphAction(paragraph, !paragraph.expanded)
    )

  def listTopics(p: Paragraph, props: Props) =
    p.topics.map{topic=>
      TopicCmp(
        globalScope = props.globalScope,
        topic = topic
      )
    }

  def createNewTopicDiag(p: Paragraph, props: Props, closeDiag: Callback) =
    TopicForm(
      formData = Forms.topicForm.formData(
        props.globalScope.pageParams.headerParams.language,
        Topic(paragraphId = p.id),
        props.globalScope.pageParams.createTopicUrl
      ),
      cancelled = closeDiag,
      submitComplete = topic => closeDiag >> props.globalScope.topicCreated(topic),
      textFieldLabel = "New topic",
      globalScope = props.globalScope,
      submitButtonName = "Create"
    )


}