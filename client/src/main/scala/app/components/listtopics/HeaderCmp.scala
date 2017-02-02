package app.components.listtopics

import app.components.Button
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import shared.forms.Forms

object HeaderCmp {

  protected case class Props(globalScope: GlobalScope)

  protected case class State(newParagraphFormOpened: Boolean = false)

  def apply(globalScope: GlobalScope) = comp(Props(globalScope))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState(State())
    .renderPS { ($, p, s) =>
      <.div(
        <.div(
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
              submitComplete = par => $.modState(_.copy(newParagraphFormOpened = false)) >> p.globalScope.paragraphCreated(par),
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