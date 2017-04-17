package app.components

import japgolly.scalajs.react.{Callback, ScalaComponent}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.Ajax
import shared.messages.{Language, Languages}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue


object LanguageSelector {

  protected case class Props(currLang: Language, supportedLanguages: List[Language], url: String, onChange: Language => Callback)

  def apply(currLang: Language, supportedLanguages: List[Language] = Languages.supportedLanguages,
            url: String,
            onChange: Language => Callback) =
    comp(Props(currLang, supportedLanguages, url, onChange))

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
                      Ajax.post(
                        url = props.url,
                        data = lang.code
                      ).map(resp => println(s"change language response = '${resp.responseText}'"))
                      props.onChange(lang)
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
