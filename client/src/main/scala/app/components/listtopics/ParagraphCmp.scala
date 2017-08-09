package app.components.listtopics

import app.components.{Checkbox, WindowFunc}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import shared.SharedConstants.{HIGHLIGHTED, HIGHLIGHT_CHILD_SPAN_ON_HOVER, PARAGRAPH_NAME}
import shared.dto.{Paragraph, Topic}

// TODO: display average score near each paragraph
// TODO: tags for paragraphs
object ParagraphCmp {

  case class Props(paragraph: Paragraph,
                   ctx: WindowFunc with ListTopicsPageContext,
                   tagFilter: String,
                   selected: Boolean) {
    @inline def render = comp.withKey("par-" + paragraph.id.get.toString)(this)
  }

  protected case class State(editMode: Boolean = false,
                             createTopicDiagOpened: Boolean = false,
                             newParagraphFormOpened: Boolean = false)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    private val checkUncheckAllBtnSize = "16px"

    def render(implicit props: Props, state: State) =
      <.div(^.`class` := ParagraphCmp.getClass.getSimpleName /*+ (if (props.paragraph.checked) " checked" else "")*/,
        if (!state.editMode) {
          <.div(^.`class` := PARAGRAPH_NAME + " " + HIGHLIGHT_CHILD_SPAN_ON_HOVER,
            ^.onClick --> props.ctx.selectParagraphAction(props.paragraph.id.get, !props.selected),
            if (props.ctx.listTopicsPageMem.selectMode) checkboxForParagraph(props) else EmptyVdom,
            <.span(
              ^.`class`:=HIGHLIGHTED,
              props.paragraph.name
            ),
//            checkAllTopicsButton(props.paragraph, props),
//            uncheckAllTopicsButton(props.paragraph, props),
            ParagraphActionsCmp.Props(
              ctx = props.ctx,
              paragraph = props.paragraph,
              onEdit = $.modState(_.copy(editMode = true)),
              onCreateParagraph = $.modState(_.copy(newParagraphFormOpened = true)),
              onCreateTopic = $.modState(_.copy(createTopicDiagOpened = true))
            ).render,
            if (state.createTopicDiagOpened) {
              createNewTopicDiag(props.paragraph, props, $.modState(_.copy(createTopicDiagOpened = false)))
            } else if (state.newParagraphFormOpened) {
              createNewParagraphDiag
            } else EmptyVdom
          )
        } else {
          ParagraphForm.Props(
            ctx = props.ctx,
            paragraph = props.paragraph,
            submitFunction = par => props.ctx.wsClient.post(
              _.updateParagraph(par),
              th => props.ctx.openOkDialog("Error updating paragraph: " + th.getMessage)
            ),
            cancelled = $.modState(_.copy(editMode = false)),
            submitComplete = par => $.modState(_.copy(editMode = false)) >> props.ctx.paragraphUpdated(par),
            textFieldLabel = "",
            submitButtonName = "Save"
          ).render
        }
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


    def checkboxForParagraph(props: Props) = Checkbox.Props(
      onChange = newVal => props.ctx.selectParagraphAction(props.paragraph.id.get, newVal),
      checked = props.selected
    ).render

    def createNewTopicDiag(p: Paragraph, props: Props, closeDiag: Callback) =
      TopicForm.Props(
        topic = Topic(paragraphId = p.id.get),
        submitFunction = topic => props.ctx.wsClient.post(
          _.createTopic(topic),
          th => props.ctx.openOkDialog("Error creating topic: " + th.getMessage)
        ),
        cancelled = closeDiag,
        submitComplete = topic => closeDiag >> props.ctx.topicCreated(topic),
        textFieldLabel = "New topic:",
        ctx = props.ctx,
        submitButtonName = "Create"
      ).render

    def createNewParagraphDiag(implicit p: Props) =
      ParagraphForm.Props(
        ctx = p.ctx,
        paragraph = Paragraph(name = "", paragraphId = p.paragraph.id),
        submitFunction = paragraph => p.ctx.wsClient.post(
          _.createParagraph(paragraph),
          th => p.ctx.openOkDialog("Error creating paragraph: " + th.getMessage)
        ),
        cancelled = $.modState(_.copy(newParagraphFormOpened = false)),
        submitComplete = par =>
          $.modState(_.copy(newParagraphFormOpened = false)) >>
            p.ctx.paragraphCreated(par) >>
            p.ctx.closeWaitPane,
        textFieldLabel = "New paragraph:",
        submitButtonName = "Create"
      ).render
  }

}