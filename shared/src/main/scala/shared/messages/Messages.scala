package shared.messages

import shared.messages.Languages.{EN, PL, RU}

object Messages {
  def title(implicit lang: Language) = Msg(PL -> "Nazwa", RU -> "Название", EN -> "Title")
  def content(implicit lang: Language) = Msg(PL -> "Text", RU -> "Текст", EN -> "Content")

  def maxLength(max: Int)(implicit lang: Language) = Msg(
    PL -> s"maksymalna długość: $max",
    RU -> s"максимальная длина: $max",
    EN -> s"maximum length: $max"
  )

  def fieldShouldNotBeEmpty(implicit lang: Language) = Msg(
    RU -> s"поле должно быть заполнено",
    EN -> s"field should not be empty"
  )

  def fieldShouldContainOnlyDigits(implicit lang: Language) = Msg(
    RU -> s"поле должно содержать только цифры",
    EN -> s"field should contain only digits"
  )
}
