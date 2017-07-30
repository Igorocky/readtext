package db

import db.TestTables.{directoryTable, itemTable, taggTable}
import slick.jdbc.H2Profile.api._

class DaoCommonChangeParentOrderedTest extends DaoCommonTestHelper {

  def createItem(parentId: Long, order: Int) = db.run(
    itemTable returning itemTable.map(_.id) += Item(directoryId = parentId, order = order, value = "i")
  ).futureValue

  def getItem(id: Long): Item = db.run(itemTable.filter(_.id === id).result.head).futureValue

  def createDir(parentId: Option[Long], order: Int): Option[Long] = Some(db.run(
    directoryTable returning directoryTable.map(_.id) += Directory(parentId = parentId, order = order, name = "d")
  ).futureValue)

  def getDir(id: Option[Long]): Directory = db.run(directoryTable.filter(_.id === id).result.head).futureValue

  "changeParentOrdered should move to the specified parent" in {
    //given
    val rootDir = createDir(None, 0)

    val d1 = createDir(rootDir, 0)
    val i1 = createItem(d1.get, 0)
    val i2 = createItem(d1.get, 1)

    val d2 = createDir(rootDir, 1)
    val i3 = createItem(d2.get, 0)
    val i4 = createItem(d2.get, 1)
    val i5 = createItem(d2.get, 2)
    val i6 = createItem(d2.get, 3)

    val d3 = createDir(rootDir, 2)

    val itemToBeMoved = getItem(i4)

    //when
    db.run(
      dao.changeParentOrdered(itemTable, i4, d1)
    ).futureValue

    //then
    getItem(i4).directoryId should be(d1.get)
  }

  "changeParentOrdered should assign new order to the moved element" in {
    //given
    val rootDir = createDir(None, 0)

    val d1 = createDir(rootDir, 0)
    val i1 = createItem(d1.get, 0)
    val i2 = createItem(d1.get, 1)

    val d2 = createDir(rootDir, 1)
    val i3 = createItem(d2.get, 0)
    val i4 = createItem(d2.get, 1)
    val i5 = createItem(d2.get, 2)
    val i6 = createItem(d2.get, 3)

    val d3 = createDir(rootDir, 2)

    val itemToBeMoved = getItem(i4)

    //when
    val newOrder = db.run(
      dao.changeParentOrdered(itemTable, i4, d1)
    ).futureValue

    //then
    newOrder should be(2)
    getItem(i4).order should be(2)
  }

  "changeParentOrdered should change order of the previous siblings of the moved element" in {
    //given
    val rootDir = createDir(None, 0)

    val d1 = createDir(rootDir, 0)
    val i1 = createItem(d1.get, 0)
    val i2 = createItem(d1.get, 1)

    val d2 = createDir(rootDir, 1)
    val i3 = createItem(d2.get, 0)
    val i4 = createItem(d2.get, 1)
    val i5 = createItem(d2.get, 2)
    val i6 = createItem(d2.get, 3)

    val d3 = createDir(rootDir, 2)

    val itemToBeMoved = getItem(i4)

    //when
    db.run(
      dao.changeParentOrdered(itemTable, i4, d1)
    ).futureValue

    //then
    getItem(i5).order should be(1)
    getItem(i6).order should be(2)
  }

  "changeParentOrdered should be able to set parentId == null" in {
    //given
    val rootDir = createDir(None, 0)

    val d1 = createDir(rootDir, 0)
    val d2 = createDir(rootDir, 1)
    val d3 = createDir(rootDir, 2)

    val itemToBeMoved = getDir(d2)

    //when
    val newOrder = db.run(
      dao.changeParentOrdered(directoryTable, d2.get, None)
    ).futureValue

    //then
    newOrder should be(1)
    val d2AfterMove = getDir(d2)
    d2AfterMove.parentId should be(None)
    d2AfterMove.order should be(1)
    getDir(d3).order should be(1)
  }
}
