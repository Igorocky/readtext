package shared.dto

import java.time.ZonedDateTime

import shared.utils.Enum

case class Card(id: Option[Long] = None,
                cardType: CardType,
                folderId: Long,
                created: ZonedDateTime,
                questionId: Long,
                answerId: Long,
                order: Int
               )

case class CardType(id: Int)

object CardTypes extends Enum[CardType] {
  private def cardType(id: Int) = addElem(CardType(id))

  val text2text = cardType(1)
  end
}
