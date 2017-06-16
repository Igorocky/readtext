package shared.api

import shared.dto.{Paragraph, Topic}

trait TopicApi {
  def loadTopicsByParentId(parentId: Long): List[Topic]
  def loadParagraphsByParentId(parentId: Option[Long]): List[Paragraph]
}
