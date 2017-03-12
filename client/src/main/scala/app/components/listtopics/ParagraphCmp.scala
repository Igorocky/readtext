package app.components.listtopics

import app.Utils.post
import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import shared.SharedConstants.{HIGHLIGHT_CHILD_SPAN_ON_HOVER, PARAGRAPH_NAME}
import shared.dto.{Paragraph, ParagraphUpdate, Topic}
import shared.forms.Forms
import upickle.default.read

import scala.util.{Failure, Success}

object ParagraphCmp {
  protected case class Props(paragraph: Paragraph,
                             globalScope: GlobalScope)

  protected case class State(editMode: Boolean = false,
                             createTopicDiagOpened: Boolean = false)

  def apply(paragraph: Paragraph, globalScope: GlobalScope) = comp.withKey(paragraph.id.get)(Props(paragraph, globalScope))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) =
      <.div(^.`class` := ParagraphCmp.getClass.getSimpleName + (if (props.paragraph.checked) " checked" else ""),
        if (!state.editMode) {
          <.div(^.`class` := PARAGRAPH_NAME + " " + HIGHLIGHT_CHILD_SPAN_ON_HOVER,
            checkboxForParagraph(props.paragraph, props),
            expandParagraphButton(props.paragraph, props),
            <.span(props.paragraph.name),
            checkAllTopicsButton(props.paragraph, props),
            uncheckAllTopicsButton(props.paragraph, props),
            editParagraphButton(props.paragraph),
            createTopicButton(props.paragraph),
            moveUpButton(props.paragraph, props),
            moveDownButton(props.paragraph, props),
            deleteParagraphButton(props, props.paragraph, props.globalScope.paragraphDeleted(props.paragraph.id.get)),
            if (state.createTopicDiagOpened) {
              createNewTopicDiag(props.paragraph, props, $.modState(_.copy(createTopicDiagOpened = false)))
            } else EmptyTag
          )
        } else {
          ParagraphForm(
            globalScope = props.globalScope,
            formData = Forms.paragraphForm.formData(
              props.globalScope.pageParams.headerParams.language,
              props.paragraph,
              props.globalScope.pageParams.updateParagraphUrl
            ),
            cancelled = $.modState(_.copy(editMode = false)),
            submitComplete = str =>
              $.modState(_.copy(editMode = false)) >>
                props.globalScope.paragraphUpdated(read[ParagraphUpdate](str)),
            textFieldLabel = "",
            submitButtonName = "Save"
          )
        },
        if (props.paragraph.expanded) listTopics(props.paragraph, props) else EmptyTag
      )

    def editParagraphButton(paragraph: Paragraph) =
      Button(
        id = "edit-paragraph-btn-" + paragraph.id.get,
        name = "Rename",
        onClick = $.modState(_.copy(editMode = true))
      )

    def checkAllTopicsButton(paragraph: Paragraph, props: Props) =
      Button(
        id = "checkAllTopicsButton-" + paragraph.id.get,
        name = "Check all",
        onClick = props.globalScope.checkTopicsAction(paragraph.topics.map(t => (t.id.get, true)))
      )

    def uncheckAllTopicsButton(paragraph: Paragraph, props: Props) =
      Button(
        id = "uncheckAllTopicsButton-" + paragraph.id.get,
        name = "Uncheck all",
        onClick = props.globalScope.checkTopicsAction(paragraph.topics.map(t => (t.id.get, false)))
      )

    def createTopicButton(paragraph: Paragraph) =
      Button(
        id = "create-topic-btn-" + paragraph.id.get,
        name = "Create topic",
        onClick = $.modState(_.copy(createTopicDiagOpened = true))
      )

    def deleteParagraphButton(props: Props, paragraph: Paragraph, onDeleted: Callback) =
      Button(
        id = "delete-paragraph-btn-" + paragraph.id.get,
        name = "Delete paragraph",
        onClick = props.globalScope.openOkCancelDialog1(
          s"Delete paragraph '${paragraph.name}'?",
          post(props.globalScope.pageParams.deleteParagraphUrl, paragraph.id.get.toString){
            case Success(_) => onDeleted
            case Failure(th) => props.globalScope.openOkDialog("Could not delete paragraph: " + th.getMessage)
          }.void
        )
      )

  def checkboxForParagraph(paragraph: Paragraph, props: Props) =
    Checkbox(
      id = "selectPar-" + paragraph.id.get,
      onChange = props.globalScope.checkParagraphAction(paragraph.id.get, _),
      checked = paragraph.checked
    )

  def expandParagraphButton(paragraph: Paragraph, props: Props) =
    Button(
      id = "expandPar-" + paragraph.id.get,
      name = if (paragraph.expanded) "-" else "+",
      onClick = props.globalScope.expandParagraphsAction(List((paragraph.id.get, !paragraph.expanded)))
    )

    def moveUpButton(paragraph: Paragraph, props: Props) =
      Button(
        id = "move-up-Par-" + paragraph.id.get,
        name = "Up",
        onClick = props.globalScope.moveUpParagraphAction(paragraph.id.get)
      )

    def moveDownButton(paragraph: Paragraph, props: Props) =
      Button(
        id = "move-down-Par-" + paragraph.id.get,
        name = "Down",
        onClick = props.globalScope.moveDownParagraphAction(paragraph.id.get)
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
        submitComplete = str => closeDiag >> props.globalScope.topicCreated(read[Topic](str)),
        textFieldLabel = "New topic",
        globalScope = props.globalScope,
        submitButtonName = "Create"
      )
  }

}