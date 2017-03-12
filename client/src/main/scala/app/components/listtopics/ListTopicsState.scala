package app.components.listtopics

import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.messages.Language

case class ListTopicsState(globalScope: GlobalScope = null,
                           waitPane: Boolean = false,
                           okDiagText: Option[String] = None,
                           okCancelDiagText: Option[String] = None,
                           onOk: Callback = Callback.empty,
                           onCancel: Callback = Callback.empty,
                           pasteListeners: Map[(Long,Int), File => Callback] = Map()) {

  def paragraphs = globalScope.pageParams.paragraphs

  def registerListener(id: Long, listener: File => Callback): ListTopicsState = {
    val order = if (pasteListeners.isEmpty) 1 else pasteListeners.map(_._1._2).max + 1
    copy(pasteListeners = pasteListeners + ((id,order) -> listener))
  }

  def runPasteListener(file: File): Unit =
    if (pasteListeners.isEmpty) () else pasteListeners.maxBy(_._1._2)._2(file).runNow()

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

  def checkTopics(ids: List[(Long, Boolean)]): ListTopicsState =
    ids.foldLeft(this){case (s,t) => s.checkTopic(t._1, t._2)}

  def checkTopic(topId: Long, newChecked: Boolean): ListTopicsState =
    modTopicById(topId, _.copy(checked = newChecked))

  def expandParagraph(id: Long, newExpanded: Boolean): ListTopicsState =
    modParagraphById(id, _.copy(expanded = newExpanded))

  def expandParagraphs(ids: List[(Long, Boolean)]): ListTopicsState =
    ids.foldLeft(this){case (s,t) => s.expandParagraph(t._1, t._2)}

  def moveUpTopic(id: Long): ListTopicsState = {
    val par = paragraphs.find(_.topics.exists(_.id.get == id)).get
    modParagraphById(par.id.get, _.copy(topics = moveUp(id, par.topics)))
  }

  def moveUpParagraph(id: Long): ListTopicsState = updateParagraphs(moveUp(id, paragraphs))

  def moveDownParagraph(id: Long): ListTopicsState = updateParagraphs(moveDown(id, paragraphs))

  def moveDownTopic(id: Long): ListTopicsState = {
    val par = paragraphs.find(_.topics.exists(_.id.get == id)).get
    modParagraphById(par.id.get, _.copy(topics = moveDown(id, par.topics)))
  }

  private def moveUp[E <: {val id: Option[Long]}](id: Long, elems: List[E]): List[E] = {
    val idx = elems.indexWhere(_.id.get == id)
    if (idx == 0) elems else {
      val vec = elems.toVector
      vec.updated(idx - 1, vec(idx)).updated(idx, vec(idx - 1)).toList
    }
  }

  private def moveDown[E <: {val id: Option[Long]}](id: Long, elems: List[E]): List[E] = {
    val idx = elems.indexWhere(_.id.get == id)
    if (idx == elems.size - 1) elems else {
      val vec = elems.toVector
      vec.updated(idx + 1, vec(idx)).updated(idx, vec(idx + 1)).toList
    }
  }

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
