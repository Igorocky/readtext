package shared.messages

case class Language(code: String)

object Languages {
  val EN = Language("EN")
  val PL = Language("PL")
  val RU = Language("RU")

  val supportedLanguages = List(EN, PL, RU)
  val fromString = supportedLanguages.map(lang => (lang.code -> lang)).toMap
}