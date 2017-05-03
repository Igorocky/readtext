package app.components.listtopics

import app.Utils._
import app.components.Checkbox
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import shared.SharedConstants.{HIGHLIGHTED, HIGHLIGHT_CHILD_SPAN_ON_HOVER, PARAGRAPH_NAME}
import shared.dto.{Paragraph, ParagraphUpdate, Topic}
import shared.forms.Forms
import upickle.default.read

import scala.util.{Failure, Success}

object ParagraphCmp {
  protected case class Props(paragraph: Paragraph,
                             globalScope: GlobalScope,
                             tagFilter: String)

  protected case class State(editMode: Boolean = false,
                             createTopicDiagOpened: Boolean = false)

  def apply(paragraph: Paragraph, globalScope: GlobalScope, tagFilter: String) =
    comp.withKey(paragraph.id.get.toString)(Props(paragraph, globalScope, tagFilter))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    private val checkUncheckAllBtnSize = "16px"

    def render(props: Props, state: State) =
      <.div(^.`class` := ParagraphCmp.getClass.getSimpleName + (if (props.paragraph.checked) " checked" else ""),
        if (!state.editMode) {
          <.div(^.`class` := PARAGRAPH_NAME + " " + HIGHLIGHT_CHILD_SPAN_ON_HOVER,
            checkboxForParagraph(props.paragraph, props),
            expandParagraphButton(props.paragraph, props),
            <.span(
              ^.`class`:=HIGHLIGHTED,
              props.paragraph.name
            ),
            checkAllTopicsButton(props.paragraph, props),
            uncheckAllTopicsButton(props.paragraph, props),
            editParagraphButton(props.paragraph),
            createTopicButton(props.paragraph),
            moveUpButton(props.paragraph, props),
            moveDownButton(props.paragraph, props),
            deleteParagraphButton(props, props.paragraph, props.globalScope.paragraphDeleted(props.paragraph.id.get)),
            if (state.createTopicDiagOpened) {
              createNewTopicDiag(props.paragraph, props, $.modState(_.copy(createTopicDiagOpened = false)))
            } else EmptyVdom
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
        if (props.paragraph.expanded) listTopics(props.paragraph, props) else EmptyVdom
      )

    def editParagraphButton(paragraph: Paragraph) = buttonWithIcon(
      onClick = $.modState(_.copy(editMode = true)),
      btnType = BTN_INFO,
      iconType = "fa-pencil-square-o"
    )

    def checkAllTopicsButton(paragraph: Paragraph, props: Props) = buttonWithImage(
      onClick = props.globalScope.checkTopicsAction(paragraph.topics.map(t => (t.id.get, true))),
      btnType = BTN_INFO,
      imgUrl = "assets/images/check-all.png",
      imgSize = checkUncheckAllBtnSize
    )

    def uncheckAllTopicsButton(paragraph: Paragraph, props: Props) = buttonWithImage(
      onClick = props.globalScope.checkTopicsAction(paragraph.topics.map(t => (t.id.get, false))),
      btnType = BTN_INFO,
      imgUrl = "assets/images/uncheck-all.png",
      imgSize = checkUncheckAllBtnSize
    )

    def createTopicButton(paragraph: Paragraph) = buttonWithText(
      onClick = $.modState(_.copy(createTopicDiagOpened = true)),
      btnType = BTN_INFO,
      text = "Create topic"
    )

    def deleteParagraphButton(props: Props, paragraph: Paragraph, onDeleted: Callback) = buttonWithIcon(
      onClick = props.globalScope.openOkDialog1(
        s"Delete paragraph '${paragraph.name}'?",
        post(props.globalScope.pageParams.deleteParagraphUrl, paragraph.id.get.toString){
          case Success(_) => onDeleted
          case Failure(th) => props.globalScope.openOkDialog("Could not delete paragraph: " + th.getMessage)
        }.void
      ),
      btnType = BTN_DANGER,
      iconType = "fa-trash-o"
    )

  def checkboxForParagraph(paragraph: Paragraph, props: Props) =
    Checkbox(
      id = "selectPar-" + paragraph.id.get,
      onChange = props.globalScope.checkParagraphAction(paragraph.id.get, _),
      checked = paragraph.checked
    )

  def expandParagraphButton(paragraph: Paragraph, props: Props) = buttonWithIcon(
    onClick = props.globalScope.expandParagraphsAction(List((paragraph.id.get, !paragraph.expanded))),
    btnType = BTN_LINK,
    iconType = if (paragraph.expanded) "fa-minus-square" else "fa-plus-square"
  )

    def moveUpButton(paragraph: Paragraph, props: Props) = buttonWithIcon(
      onClick = props.globalScope.moveUpParagraphAction(paragraph.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-up"
    )

    def moveDownButton(paragraph: Paragraph, props: Props) = buttonWithIcon(
      onClick = props.globalScope.moveDownParagraphAction(paragraph.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-down"
    )

    def listTopics(p: Paragraph, props: Props) =
      (if (props.tagFilter.trim.isEmpty) {
        p.topics
      } else {
        p.topics.filter(topic => props.globalScope.filterTopic(props.tagFilter, topic))
      }).toVdomArray{topic=>
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
        textFieldLabel = "New topic:",
        globalScope = props.globalScope,
        submitButtonName = "Create"
      )
  }

}