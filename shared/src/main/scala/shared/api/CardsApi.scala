package shared.api

import shared.dto.CardLearnInfo

trait CardsApi {
  def loadNextCardInfo(poolId: Long): CardLearnInfo
  def scoreSelected(cardId: Long, easiness: Int, score: Int): Unit
}
