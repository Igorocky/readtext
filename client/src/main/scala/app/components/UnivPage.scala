package app.components

import app.WsClient
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.api.SessionApi
import shared.messages.Language

object UnivPage {
  protected case class Props(language: Language,
                             sessionWsClient: WsClient[SessionApi, Callback],
                             onLanguageChange: Language => Callback,
                             content: TagMod
                            )

  def apply(language: Language, sessionWsClient: WsClient[SessionApi, Callback],
            onLanguageChange: Language => Callback,
            content: VdomElement) =
    comp(Props(language, sessionWsClient, onLanguageChange, content))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .render_P { props =>
      <.div(
        <.div(
          LanguageSelector(
            currLang = props.language
            ,sessionWsClient = props.sessionWsClient
            ,onChange = props.onLanguageChange
          )
        ),
        <.div(
          props.content
        )
      )
    }.build
}