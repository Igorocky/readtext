package shared.api

import shared.dto._

trait CardsApi {
  def loadCardStates(poolId: Long): List[TopicState]
  def updateCardState(cardId: Long, score: String): Unit
  def loadTopic(cardId: Long): Topic
}
