package utils

import shared.messages.Language

case class Session(language: Language)

object Session {
  val SESSION = "readtext-session"
}