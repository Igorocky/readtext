package app

case class LazyTreeNode(value: Option[Any] = None, children: Option[List[LazyTreeNode]] = None) {
  import LazyTreeNodeUtils._

  def setChildren(nodeSelector: LazyTreeNode => Boolean, children: List[LazyTreeNode]): LazyTreeNode = modNode(
    this,
    nodeSelector,
    _.copy(children = Some(children))
  )

  def moveUp(nodeSelector: LazyTreeNode => Boolean): LazyTreeNode = modNode(
    this,
    parentSelector(nodeSelector),
    node => node.copy(children = node.children.map(createUpMover(nodeSelector)))
  )

  def moveDown(nodeSelector: LazyTreeNode => Boolean): LazyTreeNode = modNode(
    this,
    parentSelector(nodeSelector),
    node => node.copy(children = node.children.map(createDownMover(nodeSelector)))
  )

  def removeNode(nodeSelector: LazyTreeNode => Boolean): LazyTreeNode = modNode(
    this,
    parentSelector(nodeSelector),
    parentNode => parentNode.copy(children = parentNode.children.map(_.filterNot(nodeSelector)))
  )

  def updateValue(nodeSelector: LazyTreeNode => Boolean, f: Option[Any] => Option[Any]): LazyTreeNode =
    modNode(this, nodeSelector, n => n.copy(value = f(n.value)))

  def addChild(nodeSelector: LazyTreeNode => Boolean, newChild: LazyTreeNode): LazyTreeNode =
    modNode(this, node => node.children.isDefined && nodeSelector(node), _.appendChildToThis(newChild))

  private def appendChildToThis(newChild: LazyTreeNode): LazyTreeNode = copy(children = Some(children.get:::newChild::Nil))
}

private[app] object LazyTreeNodeUtils {
  def modNode(node: LazyTreeNode, nodeSelector: LazyTreeNode => Boolean, f: LazyTreeNode => LazyTreeNode): LazyTreeNode =
    if (nodeSelector(node)) f(node) else tryChangeChildren(node, nodeSelector, f)

  def tryChangeChildren(node: LazyTreeNode, nodeSelector: LazyTreeNode => Boolean, f: LazyTreeNode => LazyTreeNode): LazyTreeNode =
    if (node.children.isEmpty) {
      node
    } else {
      tryChangeList(node.children.get, nodeSelector, f)
        .map(newChildren => node.copy(children = Some(newChildren)))
        .getOrElse(node)
    }

  def tryChangeList(list: List[LazyTreeNode],
                                 nodeSelector: LazyTreeNode => Boolean,
                                 f: LazyTreeNode => LazyTreeNode): Option[List[LazyTreeNode]] = {
    val changes = list
      .map(old => (old, modNode(old, nodeSelector, f)))
      .filter{case (old, newOne) => old ne newOne}
      .toMap
    if (changes.nonEmpty) {
      Some(list.map(old => changes.get(old).getOrElse(old)))
    } else {
      None
    }
  }

  def swap[E](list: List[E])(selector: (E, E) => Boolean): List[E] = list match {
    case Nil | _::Nil => list
    case x::y::rest if selector(x,y) => y::x::rest
    case x::xs => x::swap(xs)(selector)
  }

  def createUpMover[E](selector: E => Boolean)(list: List[E]): List[E] = swap(list)((x, y) => selector(y))
  def createDownMover[E](selector: E => Boolean)(list: List[E]): List[E] = swap(list)((x,y) => selector(x))

  def parentSelector(childSelector: LazyTreeNode => Boolean): LazyTreeNode => Boolean =
    _.children.map(_.exists(childSelector)).getOrElse(false)
}