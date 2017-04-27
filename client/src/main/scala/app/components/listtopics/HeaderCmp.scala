package app.components.listtopics

import app.Utils._
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import shared.dto.Paragraph
import shared.forms.Forms
import upickle.default.read

object HeaderCmp {

  protected case class Props(globalScope: GlobalScope, paragraphs: List[Paragraph])

  protected case class State(newParagraphFormOpened: Boolean = false)

  def apply(globalScope: GlobalScope, paragraphs: List[Paragraph]) = comp(Props(globalScope, paragraphs))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderPS { ($, p, s) =>
      <.div(^.`class`:=this.getClass.getSimpleName,
        <.div(
          buttonWithText(
            onClick = p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, true))),
            btnType = BTN_WARNING,
            text = "Expand All"
          ),
          buttonWithText(
            onClick = p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, false))),
            btnType = BTN_WARNING,
            text = "Collapse All"
          ),
          buttonWithText(
            onClick = $.modState(_.copy(newParagraphFormOpened = true)),
            btnType = BTN_WARNING,
            text = "Create paragraph"
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
          EmptyVdom
      )
    }.build

}