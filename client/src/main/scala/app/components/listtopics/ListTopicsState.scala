package app.components.listtopics

import japgolly.scalajs.react.Callback
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.messages.Language

case class ListTopicsState(globalScope: GlobalScope = null,
                           waitPane: Boolean = false,
                           okDiagText: Option[String] = None,
                           okCancelDiagText: Option[String] = None,
                           onOk: Callback = Callback.empty,
                           onCancel: Callback = Callback.empty) {

  def paragraphs = globalScope.pageParams.paragraphs

  def setLanguage(language: Language): ListTopicsState = copy(
    globalScope = globalScope.copy(
      pageParams = globalScope.pageParams.copy(
        headerParams = globalScope.pageParams.headerParams.copy(
          language = language
        )
      )
    )
  )

  def setGlobalScope(globalScope: GlobalScope): ListTopicsState = copy(globalScope = globalScope)

  def addParagraph(paragraph: Paragraph): ListTopicsState =
    updateParagraphs(paragraphs:::paragraph::Nil)

  def updateParagraph(parUpd: ParagraphUpdate): ListTopicsState = modParagraphById(parUpd.id, _.copy(name = parUpd.name))

  def deleteParagraph(id: Long): ListTopicsState = {
    val (rest, List(deletedPar)) = paragraphs.partition(_.id.get != id)
    updateParagraphs(rest.map(p => if (p.order < deletedPar.order) p else p.copy(order = p.order-1)))
  }

  def addTopic(topic: Topic): ListTopicsState =
    modParagraphById(topic.paragraphId.get, p => p.copy(topics = p.topics:::topic::Nil))

  def updateTopic(topicUpd: TopicUpdate): ListTopicsState =
    modTopicById(topicUpd.id, _.copy(title = topicUpd.title, images = topicUpd.images))

  def deleteTopic(topId: Long) = {
    val parWithTop = paragraphByTopicId(topId)
    val (rest, List(deleted)) = parWithTop.topics.partition(_.id.get != topId)
    modParagraphById(
      parWithTop.id.get,
      _.copy(topics = rest.map(t => if (t.order < deleted.order) t else t.copy(order = t.order-1)))
    )
  }

  def checkParagraph(parId: Long, newChecked: Boolean): ListTopicsState =
    modParagraphById(parId, _.copy(checked = newChecked))

  def checkTopic(topId: Long, newChecked: Boolean): ListTopicsState =
    modTopicById(topId, _.copy(checked = newChecked))

  def expandParagraph(p: Paragraph, newExpanded: Boolean): ListTopicsState =
    modParagraphById(p.id.get, _.copy(expanded = newExpanded))

  //-------------------------

  private def modParagraphById(ps: List[Paragraph], parId: Long, mod: Paragraph => Paragraph): List[Paragraph] =
    ps.map(p => if (p.id.get == parId) mod(p) else p)

  private def modParagraphById(parId: Long, mod: Paragraph => Paragraph): ListTopicsState =
    updateParagraphs(modParagraphById(paragraphs, parId, mod))

  private def paragraphByTopicId(topId: Long) = paragraphs.find(_.topics.exists(_.id.get == topId)).get

  private def modTopicById(topId: Long, mod: Topic => Topic): ListTopicsState = {
    val parWithTop = paragraphByTopicId(topId)
    modParagraphById(parWithTop.id.get, p => p.copy(topics = modTopicById(p.topics, topId, mod)))
  }

  private def modTopicById(ts: List[Topic], topId: Long, mod: Topic => Topic): List[Topic] =
    ts.map(t => if (t.id.get == topId) mod(t) else t)

  private def updateParagraphs(paragraphs: List[Paragraph]): ListTopicsState = copy(
    globalScope = globalScope.copy(
      pageParams = globalScope.pageParams.copy(
        paragraphs = paragraphs
      )
    )
  )
}
