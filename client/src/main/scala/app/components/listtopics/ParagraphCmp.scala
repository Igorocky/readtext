package app.components.listtopics

import app.Utils
import app.Utils.post
import app.components.{Button, Checkbox}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
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
    .renderPS{ ($, props, state) =>
        <.div(
          if (!state.editMode) {
            <.div(
              checkboxForParagraph(props.paragraph, props),
              expandParagraphButton(props.paragraph, props),
              props.paragraph.name,
              editParagraphButton(props.paragraph, $.modState(_.copy(editMode = true))),
              createTopicButton(props.paragraph, $.modState(_.copy(createTopicDiagOpened = true))),
              deleteParagraphButton(props, props.paragraph, props.globalScope.paragraphDeleted(props.paragraph.id.get)),
              if (state.createTopicDiagOpened) {
                createNewTopicDiag(props.paragraph, props, $.modState(_.copy(createTopicDiagOpened = false)))
              } else EmptyTag
            )
          } else {
            ParagraphForm(
              globalScope = props.globalScope,
              formData = Forms.paragraphForm.formData(
                props.globalScope.pageParams.headerParams.language, props.paragraph,
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

  def deleteParagraphButton(props: Props, paragraph: Paragraph, onDeleted: Callback) =
    Button(
      id = "delete-paragraph-btn-" + paragraph.id.get,
      name = "Delete paragraph",
      onClick = props.globalScope.openWaitPane >>
        post(props.globalScope.pageParams.deleteParagraphUrl, paragraph.id.get.toString){
          case Success(_) => onDeleted
          case Failure(th) => props.globalScope.openOkDialog("Could not delete paragraph: " + th.getMessage)
        }.void
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
      submitComplete = str => closeDiag >> props.globalScope.topicCreated(read[Topic](str)),
      textFieldLabel = "New topic",
      globalScope = props.globalScope,
      submitButtonName = "Create"
    )


}