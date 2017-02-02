package app.components.listtopics

import shared.dto.{Paragraph, Topic}
import shared.messages.Language

case class ListTopicsState(globalScope: GlobalScope = null,
                           waitPane: Boolean = false,
                           infoToShow: Option[String] = None) {

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

  def renameParagraph(id: Long, newName: String): ListTopicsState = modParagraphById(id, _.copy(name = newName))

  def addParagraph(paragraph: Paragraph): ListTopicsState =
    updateParagraphs(paragraphs:::paragraph::Nil)

  def addTopic(topic: Topic): ListTopicsState =
    modParagraphById(topic.paragraphId.get, p => p.copy(topics = p.topics:::topic::Nil))

  def updateTopic(topic: Topic): ListTopicsState =
    modParagraphById(topic.paragraphId.get, p => p.copy(topics = modTopicById(p.topics, topic.id.get, _ => topic)))

  def checkParagraph(p: Paragraph, newChecked: Boolean): ListTopicsState =
    modParagraphById(p.id.get, _.copy(checked = newChecked))

  def checkTopic(t: Topic, newChecked: Boolean): ListTopicsState =
    modTopicById(t.paragraphId.get, t.id.get, _.copy(checked = newChecked))

  def expandParagraph(p: Paragraph, newExpanded: Boolean): ListTopicsState =
    modParagraphById(p.id.get, _.copy(expanded = newExpanded))

  def modParagraphById(ps: List[Paragraph], parId: Long, mod: Paragraph => Paragraph): List[Paragraph] =
    ps.map(p => if (p.id.get == parId) mod(p) else p)

  def modParagraphById(parId: Long, mod: Paragraph => Paragraph): ListTopicsState =
    updateParagraphs(modParagraphById(paragraphs, parId, mod))

  def modTopicById(ts: List[Topic], topId: Long, mod: Topic => Topic): List[Topic] =
    ts.map(t => if (t.id.get == topId) mod(t) else t)

  def modTopicById(parId: Long, topId: Long, mod: Topic => Topic): ListTopicsState =
    updateParagraphs(
      modParagraphById(
        paragraphs, parId, p => p.copy(topics = modTopicById(p.topics, topId, mod))
      )
    )

  def updateParagraphs(paragraphs: List[Paragraph]): ListTopicsState = copy(
    globalScope = globalScope.copy(
      pageParams = globalScope.pageParams.copy(
        paragraphs = paragraphs
      )
    )
  )
}
