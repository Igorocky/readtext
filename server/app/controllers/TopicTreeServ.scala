package controllers

import shared.utils.tree.TopicTreeLike

case class TopicTreeServ(value: Option[Any] = None,
                     children: Option[List[TopicTreeServ]] = None
                    ) extends TopicTreeLike[TopicTreeServ] {
  override def newInstance(value: Option[Any], children: Option[List[TopicTreeServ]]): TopicTreeServ = TopicTreeServ(value, children)
  override def setChildren(newChildren: Option[List[TopicTreeServ]]): TopicTreeServ = copy(children = newChildren)
  override def setValue(newValue: Option[Any]): TopicTreeServ = copy(value = newValue)
}
