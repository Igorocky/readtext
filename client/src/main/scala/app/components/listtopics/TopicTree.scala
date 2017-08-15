package app.components.listtopics

import shared.utils.tree.TopicTreeLike

case class ParTopicAttrs(selected: Boolean = false,
                         showImg: Boolean = false,
                         actionsHidden: Boolean = true)

case class TopicTree(value: Option[Any] = None,
                     children: Option[List[TopicTree]] = None,
                     attrs: ParTopicAttrs = ParTopicAttrs()
                    ) extends TopicTreeLike[TopicTree] {
  override def newInstance(value: Option[Any], children: Option[List[TopicTree]]): TopicTree = TopicTree(value, children)
  override def setChildren(newChildren: Option[List[TopicTree]]): TopicTree = copy(children = newChildren)
  override def setValue(newValue: Option[Any]): TopicTree = copy(value = newValue)

  def changeAttrs(f: ParTopicAttrs => ParTopicAttrs): TopicTree = copy(attrs = f(attrs))

  def selectTopic(id: Long, selected: Boolean) = modNode(
    topicSelector(id),
    _.changeAttrs(_.copy(selected = selected))
  )

  def selectParagraph(id: Option[Long], selected: Boolean) = modNode(
    paragraphSelector(id),
    _.changeAttrs(_.copy(selected = selected))
  )
}
