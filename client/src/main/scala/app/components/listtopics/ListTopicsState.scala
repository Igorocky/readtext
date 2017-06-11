package app.components.listtopics

import app.LazyTreeNode
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.messages.Language

case class ListTopicsState(globalScope: ListTopicsPageGlobalScope = null,
                           data: LazyTreeNode = LazyTreeNode(),
                           waitPane: Boolean = false,
                           okDiagText: Option[String] = None,
                           okCancelDiagText: Option[String] = None,
                           onOk: Callback = Callback.empty,
                           onCancel: Callback = Callback.empty,
                           pasteListeners: Map[(Long, Int), File => Callback] = Map(),
                           tagFilter: String = "") {

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

  def setGlobalScope(globalScope: ListTopicsPageGlobalScope): ListTopicsState = copy(globalScope = globalScope)

  def addParagraph(paragraph: Paragraph): ListTopicsState = changeData(_.addChild(
    paragraphSelector(paragraph.paragraphId),
    LazyTreeNode(Some(paragraph), None)
  ))

  def updateParagraph(parUpd: ParagraphUpdate): ListTopicsState = changeData(_.updateValue(
    paragraphSelector(parUpd.id),
    _.map(_.asInstanceOf[Paragraph].update(parUpd))
  ))

  def deleteParagraph(id: Long): ListTopicsState = changeData(_.removeNode(paragraphSelector(id)))

  def addTopic(topic: Topic): ListTopicsState = changeData(_.addChild(
    paragraphSelector(topic.paragraphId),
    LazyTreeNode(Some(topic), None)
  ))

  def updateTopic(topicUpd: TopicUpdate): ListTopicsState = changeData(_.updateValue(
    topicSelector(topicUpd.id),
    _.map(_.asInstanceOf[Topic].update(topicUpd))
  ))

  def deleteTopic(topId: Long) = changeData(_.removeNode(topicSelector(topId)))

  def expandParagraph(id: Long, newExpanded: Boolean): ListTopicsState = changeData(_.updateValue(
    paragraphSelector(id),
    _.map(_.asInstanceOf[Paragraph].copy(expanded = newExpanded))
  ))

  def expandParagraphs(ids: List[(Long, Boolean)]): ListTopicsState =
    ids.foldLeft(this){case (s,t) => s.expandParagraph(t._1, t._2)}

  def moveUpTopic(id: Long): ListTopicsState = changeData(_.moveUp(topicSelector(id)))

  def moveUpParagraph(id: Long): ListTopicsState = changeData(_.moveUp(paragraphSelector(id)))

  def moveDownParagraph(id: Long): ListTopicsState = changeData(_.moveDown(paragraphSelector(id)))

  def moveDownTopic(id: Long): ListTopicsState = changeData(_.moveDown(topicSelector(id)))

  def setTags(topicId: Long, tags: List[String]): ListTopicsState = changeData(_.updateValue(
    topicSelector(topicId),
    _.map(_.asInstanceOf[Topic].copy(tags = tags))
  ))

  def setChildren(paragraphId: Option[Long], children: List[LazyTreeNode]): ListTopicsState = changeData(_.setChildren(
    paragraphSelector(paragraphId),
    children
  ))

  //-------------------------

  private def changeData(f: LazyTreeNode => LazyTreeNode): ListTopicsState = copy(data = f(data))

  private def paragraphSelector(idOpt: Option[Long]): LazyTreeNode => Boolean =
    if (idOpt.isEmpty) {
      node => {
        node match {
          case LazyTreeNode(None, _) => true
          case _ => false
        }
      }
    } else {
      node => {
        node match {
          case LazyTreeNode(Some(p: Paragraph), _) if p.id == idOpt => true
          case _ => false
        }
      }
    }

  private def paragraphSelector(id: Long): LazyTreeNode => Boolean = paragraphSelector(Some(id))

  private def topicSelector(idOpt: Option[Long]): LazyTreeNode => Boolean =
    node => node match {
      case LazyTreeNode(Some(t: Topic), _) if t.id == idOpt => true
      case _ => false
    }

  private def topicSelector(id: Long): LazyTreeNode => Boolean = topicSelector(Some(id))
}
