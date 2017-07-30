package shared.api

import shared.dto.{Paragraph, Topic, TopicTag}
import shared.forms.Forms.SubmitResponse

trait TopicApi {
  def loadTopicsByParentId(parentId: Long): List[Topic]
  def loadParagraphsByParentId(parentId: Option[Long]): List[Paragraph]
  def createParagraph(paragraph: Paragraph): SubmitResponse[Paragraph,Paragraph]
  def updateParagraph(paragraph: Paragraph): SubmitResponse[Paragraph,Paragraph]
  def createTopic(topic: Topic): SubmitResponse[Topic,Topic]
  def updateTopic(topic: Topic): SubmitResponse[Topic,Topic]
  def addTagForTopic(tag: TopicTag): SubmitResponse[TopicTag,List[String]]
  def deleteParagraph(id: Long): Unit
  def deleteTopic(id: Long): Unit
  def expand(ids: List[(Long, Boolean)]): Unit
  def moveUpParagraph(id: Long): Unit
  def moveUpTopic(id: Long): Unit
  def moveDownParagraph(id: Long): Unit
  def moveDownTopic(id: Long): Unit
  def removeTagFromTopic(topicId: Long, tag: String): List[String]

  def changeParagraphsParent(paragraphIds: List[Long], newParentId: Option[Long]): Unit
  def changeTopicsParent(topicIds: List[Long], newParentId: Long): Unit
}
