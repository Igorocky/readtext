package app.components.listtopics

import app.components.Button
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import shared.dto.Paragraph
import shared.forms.Forms
import upickle.default.read

object HeaderCmp {

  protected case class Props(globalScope: GlobalScope, paragraphs: List[Paragraph])

  protected case class State(newParagraphFormOpened: Boolean = false)

  def apply(globalScope: GlobalScope, paragraphs: List[Paragraph]) = comp(Props(globalScope, paragraphs))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState(State())
    .renderPS { ($, p, s) =>
      <.div(^.`class`:=this.getClass.getSimpleName,
        <.div(
          Button(
            id = "Expand-All-btn",
            name = "Expand All",
            onClick = p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, true)))
          ),
          Button(
            id = "Collapse-All-btn",
            name = "Collapse All",
            onClick = p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, false)))
          ),
          Button(
            id = "open-new-paragraph-diag-btn",
            name = "Create paragraph",
            onClick = $.modState(_.copy(newParagraphFormOpened = true))
          )
        ),
        if (s.newParagraphFormOpened)
          <.div(
            ParagraphForm(
              formData = Forms.paragraphForm.formData(
                language = p.globalScope.pageParams.headerParams.language,
                submitUrl = p.globalScope.pageParams.createParagraphUrl
              ),
              cancelled = $.modState(_.copy(newParagraphFormOpened = false)),
              submitComplete = str =>
                $.modState(_.copy(newParagraphFormOpened = false)) >>
                  p.globalScope.paragraphCreated(read[Paragraph](str)),
              textFieldLabel = "New paragraph:",
              submitButtonName = "Create",
              globalScope = p.globalScope
            )
          )
        else
          EmptyTag
      )
    }.build

}