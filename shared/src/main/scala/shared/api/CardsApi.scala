package shared.api

import shared.dto._
import shared.forms.Forms.SubmitResponse

trait CardsApi {
  def loadCardState(topicId: Long): Option[TopicState]
  def loadCardHistory(topicId: Long): List[List[String]]
  def updateCardState(cardId: Long, scoreAndComment: String): SubmitResponse[String,Unit]
  def loadTopic(cardId: Long): Topic
}
