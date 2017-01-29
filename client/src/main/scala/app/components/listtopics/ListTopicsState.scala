package app.components.listtopics

import shared.dto.{Paragraph, Topic}
import shared.messages.Language

case class ListTopicsState(
                  lang: Language,
                  doActionUrl: String,
                  paragraphs: List[Paragraph],
                  waitPane: Boolean = false,
                  errorDesc: Option[String] = None
                ) {

  def addParagraph(paragraph: Paragraph): ListTopicsState =
    copy(paragraphs = paragraphs:::paragraph::Nil)

  def checkParagraph(p: Paragraph, newChecked: Boolean): ListTopicsState =
    modParagraphById(p.id.get, _.copy(checked = newChecked))

  def checkTopic(p: Paragraph, t: Topic, newChecked: Boolean): ListTopicsState =
    modTopicById(p.id.get, t.id.get, _.copy(checked = newChecked))

  def expandParagraph(p: Paragraph, newExpanded: Boolean): ListTopicsState =
    modParagraphById(p.id.get, _.copy(expanded = newExpanded))

  def modParagraphById(ps: List[Paragraph], parId: Long, mod: Paragraph => Paragraph): List[Paragraph] =
    ps.map(p => if (p.id.get == parId) mod(p) else p)

  def modParagraphById(parId: Long, mod: Paragraph => Paragraph): ListTopicsState =
    copy(paragraphs = modParagraphById(paragraphs, parId, mod))

  def modTopicById(ts: List[Topic], topId: Long, mod: Topic => Topic): List[Topic] =
    ts.map(t => if (t.id.get == topId) mod(t) else t)

  def modTopicById(parId: Long, topId: Long, mod: Topic => Topic): ListTopicsState =
    copy(
      paragraphs = modParagraphById(
        paragraphs, parId, p => p.copy(topics = modTopicById(p.topics, topId, mod))
      )
    )
}