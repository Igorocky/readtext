package shared.utils.tree

import shared.dto.{Paragraph, Topic}
import shared.utils.Utils

// TODO: try to add union type here
trait TopicTreeLike[Repr <: TopicTreeLike[Repr]] extends LazyTreeNodeLike[Repr] {
  def newInstance(value: Option[Any], children: Option[List[Repr]]): Repr

  def addParagraph(paragraph: Paragraph): Repr = addChild(
    paragraphSelector(paragraph.paragraphId),
    newInstance(Some(paragraph), None)
  )

  def updateParagraph(par: Paragraph): Repr = updateValue(
    paragraphSelector(par.id),
    _.map(_.asInstanceOf[Paragraph].copy(name = par.name))
  )

  def relocateParagraph(parId: Long, destParId: Option[Long]): Repr = {
    val parSelector = paragraphSelector(parId)
    val parNodeToBeMoved = findNodes(parSelector).head
    removeNode(parSelector).modNode(
      paragraphSelector(destParId),
      p => {
        if (p.children == None)
          p
        else if (p.children == Some(Nil))
          p.setChildren(Some(List(parNodeToBeMoved)))
        else if (p.children.get.last.value.map(_.isInstanceOf[Paragraph]).getOrElse(false))
          p.appendChildToThis(parNodeToBeMoved)
        else if (p.children.get.head.value.map(_.isInstanceOf[Topic]).getOrElse(false))
          p.setChildren(Some(parNodeToBeMoved::p.children.get))
        else
          p.setChildren(Some(Utils.insert(p.children.get, parNodeToBeMoved) {
            case (left, right) => {
              for {
                lv <- left.value
                if lv.isInstanceOf[Paragraph]
                rv <- right.value
              } yield rv.isInstanceOf[Topic]
            }.getOrElse(false)
          }))
      }
    )
  }

  def relocateTopic(topId: Long, destParId: Long): Repr = {
    val topSelector = topicSelector(topId)
    val topNodeToBeMoved = findNodes(topSelector).head
    removeNode(topSelector).modNode(
      paragraphSelector(destParId),
      p => {
        if (p.children == None) p
        else p.appendChildToThis(topNodeToBeMoved)
      }
    )
  }

  def deleteParagraph(id: Long): Repr = removeNode(paragraphSelector(id))

  def addTopic(topic: Topic): Repr = addChild(
    paragraphSelector(topic.paragraphId),
    newInstance(Some(topic), None)
  )

  def updateTopic(topic: Topic): Repr = updateValue(
    topicSelector(topic.id),
    _.map(_.asInstanceOf[Topic].copy(title = topic.title, images = topic.images))
  )

  def deleteTopic(topId: Long): Repr = removeNode(topicSelector(topId))

  def expandParagraph(id: Long, newExpanded: Boolean): Repr = updateValue(
    paragraphSelector(id),
    _.map(_.asInstanceOf[Paragraph].copy(expanded = newExpanded))
  )

  def expandParagraphs(ids: List[(Long, Boolean)]): Repr =
    ids.foldLeft(self){case (s,(id, expanded)) => s.expandParagraph(id, expanded)}

  def moveUpTopic(id: Long): Repr = moveUp(topicSelector(id))

  def moveUpParagraph(id: Long): Repr = moveUp(paragraphSelector(id))

  def moveDownParagraph(id: Long): Repr = moveDown(paragraphSelector(id))

  def moveDownTopic(id: Long): Repr = moveDown(topicSelector(id))

  def setTags(topicId: Long, tags: List[String]): Repr = updateValue(
    topicSelector(topicId),
    _.map(_.asInstanceOf[Topic].copy(tags = tags))
  )

  def setChildren(paragraphId: Option[Long], children: List[Repr]): Repr = setChildren(
    paragraphSelector(paragraphId),
    children
  )

  def paragraphSelector(idOpt: Option[Long]): Repr => Boolean =
    if (idOpt.isEmpty) {
      node => {
        for {
          v <- node.value
          if v.isInstanceOf[Paragraph]
        } yield v.asInstanceOf[Paragraph].id == None
      }.getOrElse(node.value == None)
    } else {
      node => {
        for {
          v <- node.value
          if v.isInstanceOf[Paragraph]
        } yield v.asInstanceOf[Paragraph].id == idOpt
      }.getOrElse(false)
    }

  private def paragraphSelector(id: Long): Repr => Boolean = paragraphSelector(Some(id))

  private def topicSelector(idOpt: Option[Long]): Repr => Boolean =
    node => {
      for {
        v <- node.value
        if v.isInstanceOf[Topic]
      } yield v.asInstanceOf[Topic].id == idOpt
    }.getOrElse(false)

  def topicSelector(id: Long): Repr => Boolean = topicSelector(Some(id))
}
