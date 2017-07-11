package db

import db.TestTables.{directoryTable, itemTable, taggTable}
import slick.jdbc.H2Profile.api._

class DaoCommonDeleteOrderedTest extends DaoCommonTestHelper {

  def createItem(parentId: Long, order: Int) = db.run(
    itemTable returning itemTable.map(_.id) += Item(directoryId = parentId, order = order, value = "i")
  ).futureValue

  def createItems = {
    val rootDir = createDir(None, 0).get

    (createItem(rootDir, 0), createItem(rootDir, 1), createItem(rootDir, 2), createItem(rootDir, 3))
  }

  def orderOfItem(id: Long) = db.run(itemTable.filter(_.id === id).map(_.order).result.head).futureValue
  def numberOfItems = db.run(itemTable.size.result).futureValue

  def createDir(parentId: Option[Long], order: Int): Option[Long] = Some(db.run(
    directoryTable returning directoryTable.map(_.id) += Directory(parentId = parentId, order = order, name = "d")
  ).futureValue)

  def createDirsWithParent = {
    val rootDir = createDir(None, 0)
    (createDir(rootDir, 0), createDir(rootDir, 1), createDir(rootDir, 2), createDir(rootDir, 3))
  }

  def createDirsWithoutParent = {
    (createDir(None, 0), createDir(None, 1), createDir(None, 2), createDir(None, 3))
  }

  def orderOfDir(id: Option[Long]) = db.run(directoryTable.filter(_.id === id).map(_.order).result.head).futureValue
  def numberOfDirs = db.run(directoryTable.size.result).futureValue

  def createTagg(order: Int) = db.run(
    taggTable returning taggTable.map(_.id) += Tagg(order = order, value = "t")
  ).futureValue

  def createTaggs = {
    (createTagg(0), createTagg(1), createTagg(2), createTagg(3))
  }

  def orderOfTagg(id: Long) = db.run(taggTable.filter(_.id === id).map(_.order).result.head).futureValue
  def numberOfTaggs = db.run(taggTable.size.result).futureValue

  "deleteOrdered should" - {
    "M: HasParent, parentId: Some, position: lowest" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      db.run(
        dao.deleteOrdered(itemTable, i0)
      ).futureValue
      
      //then
      orderOfItem(i1) should be(0)
      orderOfItem(i2) should be(1)
      orderOfItem(i3) should be(2)
      numberOfItems should be(3)
    }
    "M: HasParent, parentId: Some, position: highest" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      db.run(
        dao.deleteOrdered(itemTable, i3)
      ).futureValue

      //then
      orderOfItem(i0) should be(0)
      orderOfItem(i1) should be(1)
      orderOfItem(i2) should be(2)
      numberOfItems should be(3)
    }
    "M: HasParent, parentId: Some, position: middle" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      db.run(
        dao.deleteOrdered(itemTable, i1)
      ).futureValue

      //then
      orderOfItem(i0) should be(0)
      orderOfItem(i2) should be(1)
      orderOfItem(i3) should be(2)
      numberOfItems should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: lowest" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      db.run(
        dao.deleteOrdered(directoryTable, d0.get)
      ).futureValue

      //then
      orderOfDir(d1) should be(0)
      orderOfDir(d2) should be(1)
      orderOfDir(d3) should be(2)
      numberOfDirs should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: highest" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      db.run(
        dao.deleteOrdered(directoryTable, d3.get)
      ).futureValue

      //then
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d2) should be(2)
      numberOfDirs should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: middle" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      db.run(
        dao.deleteOrdered(directoryTable, d2.get)
      ).futureValue

      //then
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d3) should be(2)
      numberOfDirs should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: lowest" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      db.run(
        dao.deleteOrdered(directoryTable, d0.get)
      ).futureValue

      //then
      orderOfDir(d1) should be(0)
      orderOfDir(d2) should be(1)
      orderOfDir(d3) should be(2)
      numberOfDirs should be(4)
    }
    "M: HasOptionalParent, parentId: Some, position: highest" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      db.run(
        dao.deleteOrdered(directoryTable, d3.get)
      ).futureValue

      //then
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d2) should be(2)
      numberOfDirs should be(4)
    }
    "M: HasOptionalParent, parentId: Some, position: middle" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      db.run(
        dao.deleteOrdered(directoryTable, d1.get)
      ).futureValue

      //then
      orderOfDir(d0) should be(0)
      orderOfDir(d2) should be(1)
      orderOfDir(d3) should be(2)
      numberOfDirs should be(4)
    }
    "M: no-parent, parentId: None, position: lowest" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      db.run(
        dao.deleteOrdered(taggTable, t0)
      ).futureValue

      //then
      orderOfTagg(t1) should be(0)
      orderOfTagg(t2) should be(1)
      orderOfTagg(t3) should be(2)
      numberOfTaggs should be(3)
    }
    "M: no-parent, parentId: None, position: highest" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      db.run(
        dao.deleteOrdered(taggTable, t3)
      ).futureValue

      //then
      orderOfTagg(t0) should be(0)
      orderOfTagg(t1) should be(1)
      orderOfTagg(t2) should be(2)
      numberOfTaggs should be(3)
    }
    "M: no-parent, parentId: None, position: middle" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      db.run(
        dao.deleteOrdered(taggTable, t2)
      ).futureValue

      //then
      orderOfTagg(t0) should be(0)
      orderOfTagg(t1) should be(1)
      orderOfTagg(t3) should be(2)
      numberOfTaggs should be(3)
    }
  }
}
