package app

import org.scalatest.{FlatSpec, Matchers}

class LazyTreeNodeTest extends FlatSpec with Matchers {
  private val initialTree = LazyTreeNode(
    value = Some(1),
    children = Some(List(
      LazyTreeNode(Some(2), Some(List(
        LazyTreeNode(Some(4), None), LazyTreeNode(Some(5), None)
      ))),
      LazyTreeNode(Some(3), None)
    ))
  )

  "LazyTreeNode" should "add child to node which already has children" in {
    val actualModifiedTree = initialTree.addChild(
      _.value.map(_.asInstanceOf[Int] == 1).getOrElse(false),
      LazyTreeNode(Some(4), None)
    )

    val expectedModifiedTree = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(List(
          LazyTreeNode(Some(4), None), LazyTreeNode(Some(5), None)
        ))),
        LazyTreeNode(Some(3), None),
        LazyTreeNode(Some(4), None)
      ))
    )

    actualModifiedTree should equal(expectedModifiedTree)
  }

  it should "add child to node with children == Some(Nil)" in {
    val initialTree = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(Nil)), LazyTreeNode(Some(3), None)
      ))
    )

    val actualModifiedTree = initialTree.addChild(
      _.value.map(_.asInstanceOf[Int] == 2).getOrElse(false),
      LazyTreeNode(Some(4), None)
    )

    val expectedModifiedTree = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(List(
            LazyTreeNode(Some(4), None)
        ))),
        LazyTreeNode(Some(3), None)
      ))
    )

    actualModifiedTree should equal(expectedModifiedTree)
  }

  it should "not add child to node with children == None" in {
    val actualModifiedTree = initialTree.addChild(
      _.value.map(_.asInstanceOf[Int] == 3).getOrElse(false),
      LazyTreeNode(Some(4), None)
    )

    actualModifiedTree should equal(initialTree)
  }

  it should "update value with selector by value" in {
    val actualModifiedTree = initialTree.updateValue(
      _.value.map(_ == 2).getOrElse(false),
      _ => Some(50)
    )

    val expectedModifiedTree = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(50), Some(List(
          LazyTreeNode(Some(4), None), LazyTreeNode(Some(5), None)
        ))),
        LazyTreeNode(Some(3), None)
      ))
    )

    actualModifiedTree should equal(expectedModifiedTree)
  }

  it should "update value with selector by children" in {
    val actualModifiedTree = initialTree.updateValue(
      _.children.isEmpty,
      {case Some(v: Int) => Some(v + 10)}
    )

    val expectedModifiedTree = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(List(
          LazyTreeNode(Some(14), None), LazyTreeNode(Some(15), None)
        ))),
        LazyTreeNode(Some(13), None)
      ))
    )

    actualModifiedTree should equal(expectedModifiedTree)
  }

  it should "not update value if selector doesn't match any node" in {
    val actualModifiedTree = initialTree.updateValue(
      _.value.map(_ == 50).getOrElse(false),
      _ => Some(7)
    )

    actualModifiedTree should equal(initialTree)
  }

  it should "remove existing node" in {
    val actualModifiedTree = initialTree.removeNode(
      _.value.map(_ == 5).getOrElse(false)
    )

    val expectedResult = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(List(
          LazyTreeNode(Some(4), None)
        ))),
        LazyTreeNode(Some(3), None)
      ))
    )

    actualModifiedTree should equal(expectedResult)
  }

  it should "move node down if there is space to move to" in {
    val actualModifiedTree = initialTree.moveDown(
      _.value.map(_ == 2).getOrElse(false)
    )

    val expectedResult = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(3), None),
        LazyTreeNode(Some(2), Some(List(
          LazyTreeNode(Some(4), None), LazyTreeNode(Some(5), None)
        )))
      ))
    )

    actualModifiedTree should equal(expectedResult)
  }

  it should "not move node down if there is no space to move to" in {
    val actualModifiedTree = initialTree.moveDown(
      _.value.map(_ == 3).getOrElse(false)
    )

    actualModifiedTree should equal(initialTree)
  }

  it should "move node up if there is space to move to" in {
    val actualModifiedTree = initialTree.moveUp(
      _.value.map(_ == 5).getOrElse(false)
    )

    val expectedResult = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(List(
          LazyTreeNode(Some(5), None), LazyTreeNode(Some(4), None)
        ))),
        LazyTreeNode(Some(3), None)
      ))
    )

    actualModifiedTree should equal(expectedResult)
  }

  it should "not move node up if there is no space to move to" in {
    val actualModifiedTree = initialTree.moveUp(
      _.value.map(_ == 2).getOrElse(false)
    )

    actualModifiedTree should equal(initialTree)
  }

  it should "set children for the node which doesn't have children" in {
    val actualModifiedTree = initialTree.setChildren(
      _.value.map(_ == 5).getOrElse(false),
      LazyTreeNode(Some(50), None)::LazyTreeNode(Some(60), None)::Nil
    )

    val expectedResult = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(List(
          LazyTreeNode(Some(4), None),
          LazyTreeNode(Some(5), Some(List(
            LazyTreeNode(Some(50), None), LazyTreeNode(Some(60), None)
          )))
        ))),
        LazyTreeNode(Some(3), None)
      ))
    )

    actualModifiedTree should equal(expectedResult)
  }

  it should "set children for the node which has children" in {
    val actualModifiedTree = initialTree.setChildren(
      _.value.map(_ == 2).getOrElse(false),
      Nil
    )

    val expectedResult = LazyTreeNode(
      value = Some(1),
      children = Some(List(
        LazyTreeNode(Some(2), Some(Nil)),
        LazyTreeNode(Some(3), None)
      ))
    )

    actualModifiedTree should equal(expectedResult)
  }
}
