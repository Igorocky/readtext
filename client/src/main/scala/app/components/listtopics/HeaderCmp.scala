package app.components.listtopics

import app.components.Button
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB}
import shared.dto.Paragraph
import shared.forms.Forms
import shared.messages.Language

object HeaderCmp {

  protected case class Props(
                              language: Language,
                              createParagraphUrl: String,
                              paragraphCreated: Paragraph => Callback
                            )

  protected case class State(
                              newParagraphFormOpened: Boolean = false
                            )

  def apply(language: Language, createParagraphUrl: String, paragraphCreated: Paragraph => Callback) =
    comp(Props(language, createParagraphUrl, paragraphCreated))

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
              language = p.language,
              formData = Forms.paragraphFrom.formData.copy(submitUrl = p.createParagraphUrl),
              cancelled = $.modState(_.copy(newParagraphFormOpened = false)),
              submitComplete = par => $.modState(_.copy(newParagraphFormOpened = false)) >> p.paragraphCreated(par),
              submitButtonName = "Create"
            )
          )
        else
          EmptyTag
      )
    }.build

}