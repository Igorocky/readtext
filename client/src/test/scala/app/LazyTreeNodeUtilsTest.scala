package app

import org.scalatest.{FlatSpec, Matchers}

class LazyTreeNodeUtilsTest extends FlatSpec with Matchers {

  private val initialList = List(
    LazyTreeNode(Some(1), Some(LazyTreeNode(Some(11), None)::Nil))
    ,LazyTreeNode(Some(2), Some(LazyTreeNode(Some(12), None)::Nil))
    ,LazyTreeNode(Some(3), Some(LazyTreeNode(Some(13), None)::Nil))
  )

  private def tryChangeInitialList(valueToChange: Int, newValue: Int) = LazyTreeNodeUtils.tryChangeList(
    initialList,
    {
      case n@LazyTreeNode(Some(v: Int), _) if v == valueToChange => true
      case _ => false
    },
    {
      case n@LazyTreeNode(Some(v: Int), _) if v == valueToChange => n.copy(value = Some(newValue))
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
      LazyTreeNode(Some(1), Some(LazyTreeNode(Some(11), None)::Nil))
      ,LazyTreeNode(Some(20), Some(LazyTreeNode(Some(12), None)::Nil))
      ,LazyTreeNode(Some(3), Some(LazyTreeNode(Some(13), None)::Nil))
    ))

    actualResult should equal(expectedResult)
  }

  "LazyTreeNodeUtils.tryChangeList" should "not do unnecessary changes" in {
    val actualResult = tryChangeInitialList(2, 20)

    actualResult match {
      case Some(List(
      n1@LazyTreeNode(Some(1), Some(LazyTreeNode(Some(11), None)::Nil))
      ,LazyTreeNode(Some(20), c1@Some(LazyTreeNode(Some(12), None)::Nil))
      ,n2@LazyTreeNode(Some(3), Some(LazyTreeNode(Some(13), None)::Nil))
      )) =>
        initialList match {
          case List(
          in1@LazyTreeNode(Some(1), Some(LazyTreeNode(Some(11), None)::Nil))
          ,LazyTreeNode(Some(2), ic1@Some(LazyTreeNode(Some(12), None)::Nil))
          ,in2@LazyTreeNode(Some(3), Some(LazyTreeNode(Some(13), None)::Nil))
          ) =>
            n1 shouldBe theSameInstanceAs(in1)
            n2 shouldBe theSameInstanceAs(in2)
            c1 shouldBe theSameInstanceAs(ic1)
          case _ => fail("bug in this test: initialList should match the pattern above")
        }
      case _ => fail("actual result should match pattern of expected result")
    }
  }

  "LazyTreeNodeUtils.modNode" should "return the same node if there were no changes" in {
    val initialNode = LazyTreeNode(None, Some(initialList))

    val actualResult = LazyTreeNodeUtils.modNode(
      initialNode,
      _ => false,
      _.copy(value = None)
    )

    actualResult shouldBe theSameInstanceAs(initialNode)
  }

  "LazyTreeNodeUtils.modNode" should "return correct result if there were changes" in {
    val initialNode = LazyTreeNode(None, Some(initialList))

    val selector: (Any) => Option[LazyTreeNode] = _ match {
      case n@LazyTreeNode(Some(v: Int), Some(LazyTreeNode(Some(cv), None)::Nil)) if (cv == 11 || cv == 13) => Some(n)
      case _ => None
    }

    val actualResult = LazyTreeNodeUtils.modNode(
      initialNode,
      selector andThen(_.isDefined),
      selector andThen{case Some(n@LazyTreeNode(Some(v:Int), _)) => n.copy(value = Some(v + 100))}
    )

    val expectedResult = LazyTreeNode(None, Some(List(
      LazyTreeNode(Some(101), Some(LazyTreeNode(Some(11), None)::Nil))
      ,LazyTreeNode(Some(2), Some(LazyTreeNode(Some(12), None)::Nil))
      ,LazyTreeNode(Some(103), Some(LazyTreeNode(Some(13), None)::Nil))
    )))

    actualResult should equal(expectedResult)
  }
}
