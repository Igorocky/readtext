package shared.messages

sealed trait Language {
  val code: String
}

object Languages {
  case object EN extends Language {
    override val code: String = "EN"
  }

  case object PL extends Language {
    override val code: String = "PL"
  }

  case object RU extends Language {
    override val code: String = "RU"
  }

  val supportedLanguages = List(EN, PL, RU)
  val fromString = supportedLanguages.map(lang => (lang.code -> lang)).toMap
}