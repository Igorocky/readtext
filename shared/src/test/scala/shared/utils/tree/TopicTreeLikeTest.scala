package shared.utils.tree

import org.scalatest.{FreeSpec, Matchers}
import shared.dto.Paragraph

class TopicTreeLikeTest extends FreeSpec with Matchers {

  case class TreeNodeImpl(value: Option[Any] = None, children: Option[List[TreeNodeImpl]] = None) extends TopicTreeLike[TreeNodeImpl] {
    override def newInstance(value: Option[Any], children: Option[List[TreeNodeImpl]]): TreeNodeImpl = TreeNodeImpl(value, children)
    override def setChildren(newChildren: Option[List[TreeNodeImpl]]): TreeNodeImpl = copy(children = newChildren)
    override def setValue(newValue: Option[Any]): TreeNodeImpl = copy(value = newValue)
  }


  "TopicTreeLike.setChildren should return new tree with children" in {
    //given
    val p1 = Paragraph(id = Some(1), name = "p1")
    val p2 = Paragraph(id = Some(2), name = "p2")
    val p3 = Paragraph(id = Some(3), name = "p3")
    val tree = TreeNodeImpl(Some(p1))

    //when
    val newTree = tree.setChildren(p1.id, List(TreeNodeImpl(Some(p2)), TreeNodeImpl(Some(p3))))

    //then
    (newTree eq tree) should be(false)
    newTree.children.get.map(_.value.get.asInstanceOf[Paragraph].name) should be(List("p2", "p3"))
  }
}
