package app

import app.LazyTreeNodeLikeUtils._

trait LazyTreeNodeLike[Repr <: LazyTreeNodeLike[Repr]] {
  def value: Option[Any]
  def children: Option[List[Repr]]

  def setChildren(newChildren: Option[List[Repr]]): Repr
  def setValue(newValue: Option[Any]): Repr

  def self = this.asInstanceOf[Repr]

  def setChildren(nodeSelector: Repr => Boolean, children: List[Repr]): Repr = modNode(
    nodeSelector,
    _.setChildren(Some(children))
  )

  def moveUp(nodeSelector: Repr => Boolean): Repr = modNode(
    parentSelector(nodeSelector),
    node => node.setChildren(node.children.map(createUpMover(nodeSelector)))
  )

  def moveDown(nodeSelector: Repr => Boolean): Repr = modNode(
    parentSelector(nodeSelector),
    node => node.setChildren(node.children.map(createDownMover(nodeSelector)))
  )

  def removeNode(nodeSelector: Repr => Boolean): Repr = modNode(
    parentSelector(nodeSelector),
    parentNode => parentNode.setChildren(parentNode.children.map(_.filterNot(nodeSelector)))
  )

  def updateValue(nodeSelector: Repr => Boolean, f: Option[Any] => Option[Any]): Repr =
    modNode(nodeSelector, n => n.setValue(f(n.value)))

  def addChild(nodeSelector: Repr => Boolean, newChild: Repr): Repr =
    modNode(node => node.children.isDefined && nodeSelector(node), _.appendChildToThis(newChild))

  def modNode(nodeSelector: Repr => Boolean, f: Repr => Repr): Repr = {
    val changedChildren = tryChangeChildren(self, nodeSelector, f)
    if (nodeSelector(changedChildren)) f(changedChildren) else changedChildren
  }

  def findNodes(nodeSelector: Repr => Boolean): List[Repr] = {
    val childrenResults = children.map(_.flatMap(_.findNodes(nodeSelector))).getOrElse(Nil)
    if (nodeSelector(self)) self::childrenResults
    else childrenResults
  }

  def appendChildToThis(newChild: Repr): Repr = setChildren(Some(children.get:::newChild::Nil))
}

object LazyTreeNodeLikeUtils {
  def tryChangeChildren[Repr <: LazyTreeNodeLike[Repr]](node: Repr, nodeSelector: Repr => Boolean, f: Repr => Repr): Repr =
    if (node.children.isEmpty) {
      node
    } else {
      tryChangeList(node.children.get)(nodeSelector, f)
        .map(newChildren => node.setChildren(Some(newChildren)))
        .getOrElse(node)
    }

  def tryChangeList[Repr <: LazyTreeNodeLike[Repr]](list: List[Repr])(
                    nodeSelector: Repr => Boolean,
                    f: Repr => Repr): Option[List[Repr]] = {
    val changes = list
      .map(old => (old, old.modNode(nodeSelector, f)))
      .filter{case (old, newOne) => old ne newOne}
      .toMap
    if (changes.nonEmpty) {
      Some(list.map(old => changes.get(old).getOrElse(old)))
    } else {
      None
    }
  }

  def parentSelector[Repr <: LazyTreeNodeLike[Repr]](childSelector: Repr => Boolean): Repr => Boolean =
    _.children.map(_.exists(childSelector)).getOrElse(false)

  def swap[E](list: List[E])(selector: (E, E) => Boolean): List[E] = list match {
    case Nil | _::Nil => list
    case x::y::rest if selector(x,y) => y::x::rest
    case x::xs => x::swap(xs)(selector)
  }

  def createUpMover[E](selector: E => Boolean)(list: List[E]): List[E] = swap(list)((x, y) => selector(y))
  def createDownMover[E](selector: E => Boolean)(list: List[E]): List[E] = swap(list)((x,y) => selector(x))

//  def insert[E](list: List[E], elem: E)(selector: (Option[E], Option[E]) => Boolean): List[E] = list match {
//    case Nil | _::Nil => list
//    case x::y::rest if selector(x,y) => x::elem::y::rest
//    case x::xs => x::insert(xs, elem)(selector)
//  }


}