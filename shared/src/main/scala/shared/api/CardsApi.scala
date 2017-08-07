package shared.api

import shared.dto._

trait CardsApi {
  def loadCardStates(poolId: Long): List[TopicState]
  def updateCardState(cardId: Long, easiness: Easiness, score: Score): Unit
  def loadTopic(cardId: Long): Topic
}
