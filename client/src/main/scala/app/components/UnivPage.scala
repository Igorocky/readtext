package app.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactElement}
import shared.messages.Language

object UnivPage {
  protected case class Props(language: Language, changeLangUrl: String, onLanguageChange: Language => Callback,
                             content: TagMod)

  def apply(language: Language, changeLangUrl: String,
            onLanguageChange: Language => Callback,
            content: ReactElement) =
    comp(Props(language, changeLangUrl, onLanguageChange, content))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .render_P { props =>
      <.div(
        <.div(
          LanguageSelector(
            currLang = props.language
            ,url = props.changeLangUrl
            ,onChange = props.onLanguageChange
          )
        ),
        <.div(
          props.content
        )
      )
    }.build
}