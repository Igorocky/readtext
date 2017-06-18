package app.components.listtopics

import app.Utils._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import shared.SharedConstants.{HIGHLIGHTED, HIGHLIGHT_CHILD_SPAN_ON_HOVER, PARAGRAPH_NAME}
import shared.dto.{Paragraph, Topic}

object ParagraphCmp {
  case class Props(paragraph: Paragraph,
                             globalScope: ListTopicsPageGlobalScope,
                             tagFilter: String) {
    @inline def render = comp.withKey("par-" + paragraph.id.get.toString)(this)
  }

  protected case class State(editMode: Boolean = false,
                             createTopicDiagOpened: Boolean = false)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    private val checkUncheckAllBtnSize = "16px"

    def render(props: Props, state: State) =
      <.div(^.`class` := ParagraphCmp.getClass.getSimpleName /*+ (if (props.paragraph.checked) " checked" else "")*/,
        if (!state.editMode) {
          <.div(^.`class` := PARAGRAPH_NAME + " " + HIGHLIGHT_CHILD_SPAN_ON_HOVER,
//            checkboxForParagraph(props.paragraph, props),
            <.span(
              ^.`class`:=HIGHLIGHTED,
              props.paragraph.name
            ),
//            checkAllTopicsButton(props.paragraph, props),
//            uncheckAllTopicsButton(props.paragraph, props),
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
          ParagraphForm.Props(
            globalScope = props.globalScope,
            paragraph = props.paragraph,
            submitFunction = par => props.globalScope.wsClient.post(
              _.updateParagraph(par),
              th => props.globalScope.openOkDialog("Error updating paragraph: " + th.getMessage)
            ),
            cancelled = $.modState(_.copy(editMode = false)),
            submitComplete = par => $.modState(_.copy(editMode = false)) >> props.globalScope.paragraphUpdated(par),
            textFieldLabel = "",
            submitButtonName = "Save"
          ).render
        }
      )

    def editParagraphButton(paragraph: Paragraph) = buttonWithIcon(
      onClick = $.modState(_.copy(editMode = true)),
      btnType = BTN_INFO,
      iconType = "fa-pencil-square-o"
    )

//    def checkAllTopicsButton(paragraph: Paragraph, props: Props) = buttonWithImage(
//      onClick = props.globalScope.checkTopicsAction(paragraph.topics.map(t => (t.id.get, true))),
//      btnType = BTN_INFO,
//      imgUrl = "assets/images/check-all.png",
//      imgSize = checkUncheckAllBtnSize
//    )

//    def uncheckAllTopicsButton(paragraph: Paragraph, props: Props) = buttonWithImage(
//      onClick = props.globalScope.checkTopicsAction(paragraph.topics.map(t => (t.id.get, false))),
//      btnType = BTN_INFO,
//      imgUrl = "assets/images/uncheck-all.png",
//      imgSize = checkUncheckAllBtnSize
//    )

    def createTopicButton(paragraph: Paragraph) = buttonWithText(
      onClick = $.modState(_.copy(createTopicDiagOpened = true)),
      btnType = BTN_INFO,
      text = "Create topic"
    )

    def deleteParagraphButton(props: Props, paragraph: Paragraph, onDeleted: Callback) = buttonWithIcon(
      onClick = props.globalScope.openOkDialog1(
        s"Delete paragraph '${paragraph.name}'?",
        props.globalScope.wsClient.post(
          _.deleteParagraph(paragraph.id.get),
          th => props.globalScope.openOkDialog("Could not delete paragraph: " + th.getMessage)
        ) { case () => onDeleted }
      ),
      btnType = BTN_DANGER,
      iconType = "fa-trash-o"
    )

//  def checkboxForParagraph(paragraph: Paragraph, props: Props) =
//    Checkbox(
//      id = "selectPar-" + paragraph.id.get,
//      onChange = props.globalScope.checkParagraphAction(paragraph.id.get, _),
//      checked = paragraph.checked
//    )

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

    def createNewTopicDiag(p: Paragraph, props: Props, closeDiag: Callback) =
      TopicForm.Props(
        topic = Topic(paragraphId = p.id),
        submitFunction = topic => props.globalScope.wsClient.post(
          _.createTopic(topic),
          th => props.globalScope.openOkDialog("Error creating topic: " + th.getMessage)
        ),
        cancelled = closeDiag,
        submitComplete = topic => closeDiag >> props.globalScope.topicCreated(topic),
        textFieldLabel = "New topic:",
        globalScope = props.globalScope,
        submitButtonName = "Create"
      ).render
  }

}