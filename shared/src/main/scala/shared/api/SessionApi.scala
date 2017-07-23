package shared.api

import shared.messages.Language

trait SessionApi {
  def changeLanguage(newLang: Language): Unit
}
