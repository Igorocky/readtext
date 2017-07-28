package app.components.listtopics

import app.LazyTreeNode
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.dto.{Paragraph, Topic}

case class ListTopicsPageMem(data: LazyTreeNode = LazyTreeNode(),
                             pasteListeners: Map[(Long, Int), File => Callback] = Map(),
                             tagFilter: String = "") {

  def registerListener(id: Long, listener: File => Callback) = {
    val order = if (pasteListeners.isEmpty) 1 else pasteListeners.map(_._1._2).max + 1
    copy(pasteListeners = pasteListeners + ((id,order) -> listener))
  }

  def unregisterPasteListener(id: Long) = copy(pasteListeners = pasteListeners.filterNot(_._1._2 == id))

  def addParagraph(paragraph: Paragraph) = changeData(_.addChild(
    paragraphSelector(paragraph.paragraphId),
    LazyTreeNode(Some(paragraph), None)
  ))

  def updateParagraph(par: Paragraph) = changeData(_.updateValue(
    paragraphSelector(par.id),
    _.map(_.asInstanceOf[Paragraph].copy(name = par.name))
  ))

  def deleteParagraph(id: Long) = changeData(_.removeNode(paragraphSelector(id)))

  def addTopic(topic: Topic) = changeData(_.addChild(
    paragraphSelector(topic.paragraphId),
    LazyTreeNode(Some(topic), None)
  ))

  def updateTopic(topic: Topic) = changeData(_.updateValue(
    topicSelector(topic.id),
    _.map(_.asInstanceOf[Topic].copy(title = topic.title, images = topic.images))
  ))

  def deleteTopic(topId: Long) = changeData(_.removeNode(topicSelector(topId)))

  def expandParagraph(id: Long, newExpanded: Boolean) = changeData(_.updateValue(
    paragraphSelector(id),
    _.map(_.asInstanceOf[Paragraph].copy(expanded = newExpanded))
  ))

  def expandParagraphs(ids: List[(Long, Boolean)]) =
    ids.foldLeft(this){case (s,(id, expanded)) => s.expandParagraph(id, expanded)}

  def moveUpTopic(id: Long) = changeData(_.moveUp(topicSelector(id)))

  def moveUpParagraph(id: Long) = changeData(_.moveUp(paragraphSelector(id)))

  def moveDownParagraph(id: Long) = changeData(_.moveDown(paragraphSelector(id)))

  def moveDownTopic(id: Long) = changeData(_.moveDown(topicSelector(id)))

  def setTags(topicId: Long, tags: List[String]) = changeData(_.updateValue(
    topicSelector(topicId),
    _.map(_.asInstanceOf[Topic].copy(tags = tags))
  ))

  def setChildren(paragraphId: Option[Long], children: List[LazyTreeNode]) = changeData(_.setChildren(
    paragraphSelector(paragraphId),
    children
  ))

  //-------------------------

  private def changeData(f: LazyTreeNode => LazyTreeNode) = copy(data = f(data))

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
