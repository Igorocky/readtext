package shared.messages

import shared.messages.Languages.EN

object Msg {
  def apply(translations: (Language, String)*)(implicit lang: Language): String =
    find(translations, lang)
      .orElse(find(translations, EN))
      .getOrElse(s"no translation for $lang")

  private def find(translations: Seq[(Language, String)], language: Language) =
    translations.find(_._1 == language).map(_._2)
}