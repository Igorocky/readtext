package app.components

import app.WsClient
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.api.SessionApi
import shared.messages.{Language, Languages}


object LanguageSelector {

  protected case class Props(currLang: Language,
                             supportedLanguages: List[Language],
                             sessionWsClient: WsClient[SessionApi, Callback],
                             onChange: Language => Callback
                            )

  def apply(currLang: Language, supportedLanguages: List[Language] = Languages.supportedLanguages,
            sessionWsClient: WsClient[SessionApi, Callback],
            onChange: Language => Callback) =
    comp(Props(currLang, supportedLanguages, sessionWsClient, onChange))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .render_P{props =>
      <.table(
        <.tbody(
          <.tr(
            props.supportedLanguages.toTagMod{ lang=>
              <.td(
                if (lang == props.currLang) {
                  <.b(lang.code)
                } else {
                  <.a(
                    ^.href:="#",
                    ^.onClick --> {
                      props.sessionWsClient.post(
                        _.changeLanguage(lang),
                        th => Callback(println(s"error changing language: $th"))
                      ){
                        _ => props.onChange(lang)
                      }
                    },
                    lang.code
                  )
                }
              )
            }
          )
        )
      )
    }.build
}
