package shared.api

import shared.dto._
import shared.forms.Forms.SubmitResponse

trait CardsApi {
  def loadCardStates(poolId: Long): List[(Long, Option[TopicState])]
  def updateCardState(cardId: Long, scoreAndComment: String): SubmitResponse[String,Unit]
  def loadTopic(cardId: Long): Topic
}
