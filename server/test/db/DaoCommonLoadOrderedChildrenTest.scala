package db

import db.TestTables.{directoryTable, itemTable}
import slick.jdbc.H2Profile.api._

class DaoCommonLoadOrderedChildrenTest extends DaoCommonTestHelper {

  def createItem(parentId: Long, order: Int) = {
    val item: Item = Item (directoryId = parentId, order = order, value = "i")
    val id = db.run(
      itemTable returning itemTable.map(_.id) += item
    ).futureValue
    item.copy(id = Some(id))
  }

  def createDir(parentId: Option[Long], order: Int) = {
    val dir: Directory = Directory(parentId = parentId, order = order, name = "d")
    val id = db.run(
      directoryTable returning directoryTable.map(_.id) += dir
    ).futureValue
    dir.copy(id = Some(id))
  }

  "loadOrderedChildren should" - {
    "M: HasParent, parentId: Some, siblings: yes" in {
      //given
      val dir = createDir(None, 0)
      val i2 = createItem(dir.id.get, 1)
      val i1 = createItem(dir.id.get, 0)
      val i4 = createItem(dir.id.get, 3)
      val i3 = createItem(dir.id.get, 2)

      //when
      val items = db.run(
        dao.loadOrderedChildren(itemTable, dir.id.get)
      ).futureValue.toList
      
      //then
      items should be(List(i1, i2, i3, i4))
    }
    "M: HasParent, parentId: Some, siblings: no" in {
      //given
      val dir = createDir(None, 0)
      val i2 = createItem(dir.id.get, 1)
      val i1 = createItem(dir.id.get, 0)
      val i4 = createItem(dir.id.get, 3)
      val i3 = createItem(dir.id.get, 2)

      //when
      val items = db.run(
        dao.loadOrderedChildren(itemTable, dir.id.get + 100)
      ).futureValue.toList

      //then
      items should be(Nil)
    }
    "M: HasOptionalParent, parentId: None, siblings: yes" in {
      //given
      val d1 = createDir(None, 1)
      val d3 = createDir(None, 3)
      val d2 = createDir(None, 2)
      val d0 = createDir(None, 0)

      //when
      val dirs = db.run(
        dao.loadOrderedChildren(directoryTable, None)
      ).futureValue.toList

      //then
      dirs should be(List(d0, d1, d2, d3))
    }
    "M: HasOptionalParent, parentId: None, siblings: no" in {
      //given
      //empty directory table

      //when
      val dirs = db.run(
        dao.loadOrderedChildren(directoryTable, None)
      ).futureValue.toList

      //then
      dirs should be(Nil)
    }
    "M: HasOptionalParent, parentId: Some, siblings: yes" in {
      //given
      val root = createDir(None, 0)
      val d1 = createDir(root.id, 1)
      val d3 = createDir(root.id, 3)
      val d0 = createDir(root.id, 0)
      val d2 = createDir(root.id, 2)

      //when
      val dirs = db.run(
        dao.loadOrderedChildren(directoryTable, root.id)
      ).futureValue.toList

      //then
      dirs should be(List(d0, d1, d2, d3))
    }
    "M: HasOptionalParent, parentId: Some, siblings: no" in {
      //given
      val root = createDir(None, 0)
      val d1 = createDir(root.id, 1)
      val d3 = createDir(root.id, 3)
      val d0 = createDir(root.id, 0)
      val d2 = createDir(root.id, 2)

      //when
      val dirs = db.run(
        dao.loadOrderedChildren(directoryTable, root.id.map(_ + 100))
      ).futureValue.toList

      //then
      dirs should be(Nil)
    }
  }
}
