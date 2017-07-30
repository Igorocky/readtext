package app

import org.scalatest.{FlatSpec, Matchers}

class LazyTreeNodeLikeTest extends FlatSpec with Matchers {

  case class LazyTreeNodeImpl(value: Option[Any] = None, children: Option[List[LazyTreeNodeImpl]] = None) extends LazyTreeNodeLike[LazyTreeNodeImpl] {
    override def setChildren(newChildren: Option[List[LazyTreeNodeImpl]]): LazyTreeNodeImpl = copy(children = newChildren)
    override def setValue(newValue: Option[Any]): LazyTreeNodeImpl = copy(value = newValue)
  }

  private val initialList = List(
    LazyTreeNodeImpl(Some(1), Some(LazyTreeNodeImpl(Some(11), None)::Nil))
    ,LazyTreeNodeImpl(Some(2), Some(LazyTreeNodeImpl(Some(12), None)::Nil))
    ,LazyTreeNodeImpl(Some(3), Some(LazyTreeNodeImpl(Some(13), None)::Nil))
  )

  private def tryChangeInitialList(valueToChange: Int, newValue: Int) = LazyTreeNodeLikeUtils.tryChangeList(
    initialList)(
    {
      case n@LazyTreeNodeImpl(Some(v: Int), _) if v == valueToChange => true
      case _ => false
    },
    {
      case n@LazyTreeNodeImpl(Some(v: Int), _) if v == valueToChange => n.copy(value = Some(newValue))
    }
  )

  "LazyTreeNodeUtils.tryChangeList" should "return None if there were no changes" in {
    val actualResult = tryChangeInitialList(20, 21)

    actualResult should equal(None)
  }

  "LazyTreeNodeUtils.tryChangeList" should "return Some(_) if there were changes" in {
    val actualResult = tryChangeInitialList(2, 20)

    actualResult shouldBe a [Some[_]]
  }

  "LazyTreeNodeUtils.tryChangeList" should "return correct result if there were changes" in {
    val actualResult = tryChangeInitialList(2, 20)

    val expectedResult = Some(List(
      LazyTreeNodeImpl(Some(1), Some(LazyTreeNodeImpl(Some(11), None)::Nil))
      ,LazyTreeNodeImpl(Some(20), Some(LazyTreeNodeImpl(Some(12), None)::Nil))
      ,LazyTreeNodeImpl(Some(3), Some(LazyTreeNodeImpl(Some(13), None)::Nil))
    ))

    actualResult should equal(expectedResult)
  }

  "LazyTreeNodeUtils.tryChangeList" should "not do unnecessary changes" in {
    val actualResult = tryChangeInitialList(2, 20)

    actualResult match {
      case Some(List(
      n1@LazyTreeNodeImpl(Some(1), Some(LazyTreeNodeImpl(Some(11), None)::Nil))
      ,LazyTreeNodeImpl(Some(20), c1@Some(LazyTreeNodeImpl(Some(12), None)::Nil))
      ,n2@LazyTreeNodeImpl(Some(3), Some(LazyTreeNodeImpl(Some(13), None)::Nil))
      )) =>
        initialList match {
          case List(
          in1@LazyTreeNodeImpl(Some(1), Some(LazyTreeNodeImpl(Some(11), None)::Nil))
          ,LazyTreeNodeImpl(Some(2), ic1@Some(LazyTreeNodeImpl(Some(12), None)::Nil))
          ,in2@LazyTreeNodeImpl(Some(3), Some(LazyTreeNodeImpl(Some(13), None)::Nil))
          ) =>
            n1 shouldBe theSameInstanceAs(in1)
            n2 shouldBe theSameInstanceAs(in2)
            c1 shouldBe theSameInstanceAs(ic1)
          case _ => fail("bug in this test: initialList should match the pattern above")
        }
      case _ => fail("actual result should match pattern of expected result")
    }
  }

  "LazyTreeNodeLike.modNode" should "return the same node if there were no changes" in {
    val initialNode = LazyTreeNodeImpl(None, Some(initialList))

    val actualResult = initialNode.modNode(
      _ => false,
      _.copy(value = None)
    )

    actualResult shouldBe theSameInstanceAs(initialNode)
  }

  "LazyTreeNodeLike.modNode" should "return correct result if there were changes" in {
    val initialNode = LazyTreeNodeImpl(None, Some(initialList))

    val selector: (Any) => Option[LazyTreeNodeImpl] = _ match {
      case n@LazyTreeNodeImpl(Some(v: Int), Some(LazyTreeNodeImpl(Some(cv), None)::Nil)) if (cv == 11 || cv == 13) => Some(n)
      case _ => None
    }

    val actualResult = initialNode.modNode(
      selector andThen(_.isDefined),
      selector andThen{case Some(n@LazyTreeNodeImpl(Some(v:Int), _)) => n.copy(value = Some(v + 100))}
    )

    val expectedResult = LazyTreeNodeImpl(None, Some(List(
      LazyTreeNodeImpl(Some(101), Some(LazyTreeNodeImpl(Some(11), None)::Nil))
      ,LazyTreeNodeImpl(Some(2), Some(LazyTreeNodeImpl(Some(12), None)::Nil))
      ,LazyTreeNodeImpl(Some(103), Some(LazyTreeNodeImpl(Some(13), None)::Nil))
    )))

    actualResult should equal(expectedResult)
  }

  "LazyTreeNodeLike.modNode" should "modify all matching nodes" in {
    val initialNode = LazyTreeNodeImpl(Some(7), Some(initialList))

    val selector: LazyTreeNodeImpl => Boolean = _ match {
      case n@LazyTreeNodeImpl(Some(v: Int), _) if (v == 7 || v == 3 || v == 11) => true
      case _ => false
    }

    val actualResult = initialNode.modNode(
      selector,
      n => n.copy(value = Some(n.value.get.asInstanceOf[Int] + 200))
    )

    val expectedResult = LazyTreeNodeImpl(Some(207), Some(List(
      LazyTreeNodeImpl(Some(1), Some(LazyTreeNodeImpl(Some(211), None)::Nil))
      ,LazyTreeNodeImpl(Some(2), Some(LazyTreeNodeImpl(Some(12), None)::Nil))
      ,LazyTreeNodeImpl(Some(203), Some(LazyTreeNodeImpl(Some(13), None)::Nil))
    )))

    actualResult should equal(expectedResult)
  }

  "LazyTreeNodeLike.findNodes" should "find all matching nodes" in {
    val initialNode = LazyTreeNodeImpl(Some(7), Some(initialList))

    val selector: LazyTreeNodeImpl => Boolean = _ match {
      case n@LazyTreeNodeImpl(Some(v: Int), _) if (v == 7 || v == 2 || v == 13) => true
      case _ => false
    }

    val actualResult = initialNode.findNodes(selector)

    actualResult.length should be(3)
    actualResult.exists(_.value == Some(7)) should be(true)
    actualResult.exists(_.value == Some(2)) should be(true)
    actualResult.exists(_.value == Some(13)) should be(true)
  }
}
