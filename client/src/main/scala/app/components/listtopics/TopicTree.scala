package app.components.listtopics

import shared.dto.{Paragraph, Topic}
import shared.utils.Utils
import shared.utils.tree.LazyTreeNodeLike

case class ParTopicAttrs(selected: Boolean = false,
                         showImg: Boolean = false)

case class TopicTree(value: Option[Any] = None,
                     children: Option[List[TopicTree]] = None,
                     attrs: ParTopicAttrs = ParTopicAttrs()
                    ) extends LazyTreeNodeLike[TopicTree] {
  override def setChildren(newChildren: Option[List[TopicTree]]): TopicTree = copy(children = newChildren)
  override def setValue(newValue: Option[Any]): TopicTree = copy(value = newValue)

  def changeAttrs(f: ParTopicAttrs => ParTopicAttrs): TopicTree = copy(attrs = f(attrs))

  def addParagraph(paragraph: Paragraph): TopicTree = addChild(
    paragraphSelector(paragraph.paragraphId),
    TopicTree(Some(paragraph), None)
  )

  def updateParagraph(par: Paragraph) = updateValue(
    paragraphSelector(par.id),
    _.map(_.asInstanceOf[Paragraph].copy(name = par.name))
  )

  def relocateParagraph(parId: Long, destParId: Option[Long]): TopicTree = {
    val parSelector = paragraphSelector(parId)
    val parNodeToBeMoved = findNodes(parSelector).head
    removeNode(parSelector).modNode(
      paragraphSelector(destParId),
      _ match {
        case p@TopicTree(_, None, _) => p
        case p@TopicTree(_, Some(Nil), _) => p.setChildren(Some(List(parNodeToBeMoved)))
        case p@TopicTree(_, Some(children), _) if children.last.value.map(_.isInstanceOf[Paragraph]).getOrElse(false) =>
          p.appendChildToThis(parNodeToBeMoved)
        case p@TopicTree(_, Some(children), _) if children.head.value.map(_.isInstanceOf[Topic]).getOrElse(false) =>
          p.setChildren(Some(parNodeToBeMoved::children))
        case p@TopicTree(_, Some(children), _) => p.setChildren(Some(Utils.insert(children, parNodeToBeMoved){
          case (TopicTree(Some(p:Paragraph),_,_), TopicTree(Some(t:Topic),_,_)) => true
          case _ => false
        }))
      }
    )
  }

  def relocateTopic(topId: Long, destParId: Long): TopicTree = {
    val topSelector = topicSelector(topId)
    val topNodeToBeMoved = findNodes(topSelector).head
    removeNode(topSelector).modNode(
      paragraphSelector(destParId),
      _ match {
        case p@TopicTree(_, None, _) => p
        case p@TopicTree(_, Some(_), _) =>  p.appendChildToThis(topNodeToBeMoved)
      }
    )
  }

  def deleteParagraph(id: Long) = removeNode(paragraphSelector(id))

  def addTopic(topic: Topic) = addChild(
    paragraphSelector(topic.paragraphId),
    TopicTree(Some(topic), None)
  )

  def updateTopic(topic: Topic) = updateValue(
    topicSelector(topic.id),
    _.map(_.asInstanceOf[Topic].copy(title = topic.title, images = topic.images))
  )

  def selectTopic(id: Long, selected: Boolean) = modNode(
    topicSelector(id),
    _.changeAttrs(_.copy(selected = selected))
  )

  def selectParagraph(id: Option[Long], selected: Boolean) = modNode(
    paragraphSelector(id),
    _.changeAttrs(_.copy(selected = selected))
  )

  def deleteTopic(topId: Long) = removeNode(topicSelector(topId))

  def expandParagraph(id: Long, newExpanded: Boolean) = updateValue(
    paragraphSelector(id),
    _.map(_.asInstanceOf[Paragraph].copy(expanded = newExpanded))
  )

  def expandParagraphs(ids: List[(Long, Boolean)]) =
    ids.foldLeft(this){case (s,(id, expanded)) => s.expandParagraph(id, expanded)}

  def moveUpTopic(id: Long) = moveUp(topicSelector(id))

  def moveUpParagraph(id: Long) = moveUp(paragraphSelector(id))

  def moveDownParagraph(id: Long) = moveDown(paragraphSelector(id))

  def moveDownTopic(id: Long) = moveDown(topicSelector(id))

  def setTags(topicId: Long, tags: List[String]) = updateValue(
    topicSelector(topicId),
    _.map(_.asInstanceOf[Topic].copy(tags = tags))
  )

  def setChildren(paragraphId: Option[Long], children: List[TopicTree]): TopicTree = setChildren(
    paragraphSelector(paragraphId),
    children
  )

  private def paragraphSelector(idOpt: Option[Long]): TopicTree => Boolean =
    if (idOpt.isEmpty) {
      node => {
        node match {
          case TopicTree(None, _, _) => true
          case TopicTree(Some(p:Paragraph), _, _) if p.id == None => true
          case _ => false
        }
      }
    } else {
      node => {
        node match {
          case TopicTree(Some(p: Paragraph), _, _) if p.id == idOpt => true
          case _ => false
        }
      }
    }

  private def paragraphSelector(id: Long): TopicTree => Boolean = paragraphSelector(Some(id))

  private def topicSelector(idOpt: Option[Long]): TopicTree => Boolean =
    node => node match {
      case TopicTree(Some(t: Topic), _, _) if t.id == idOpt => true
      case _ => false
    }

  def topicSelector(id: Long): TopicTree => Boolean = topicSelector(Some(id))
}
