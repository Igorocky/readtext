package app.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.messages.Language

object UnivPage {
  protected case class Props(language: Language, changeLangUrl: String, onLanguageChange: Language => Callback,
                             content: TagMod)

  def apply(language: Language, changeLangUrl: String,
            onLanguageChange: Language => Callback,
            content: VdomElement) =
    comp(Props(language, changeLangUrl, onLanguageChange, content))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
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