package db

import db.TestTables.{directoryTable, itemTable, taggTable}
import slick.jdbc.H2Profile.api._

class DaoCommonChangeOrderTest extends DaoCommonTestHelper {

  def createItem(parentId: Long, order: Int) = db.run(
    itemTable returning itemTable.map(_.id) += Item(directoryId = parentId, order = order, value = "i")
  ).futureValue

  def createItems = {
    val rootDir = createDir(None, 0).get

    (createItem(rootDir, 0), createItem(rootDir, 1), createItem(rootDir, 2), createItem(rootDir, 3))
  }

  def orderOfItem(id: Long) = db.run(itemTable.filter(_.id === id).map(_.order).result.head).futureValue

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

  def createTagg(order: Int) = db.run(
    taggTable returning taggTable.map(_.id) += Tagg(order = order, value = "t")
  ).futureValue

  def createTaggs = {
    (createTagg(0), createTagg(1), createTagg(2), createTagg(3))
  }

  def orderOfTagg(id: Long) = db.run(taggTable.filter(_.id === id).map(_.order).result.head).futureValue

  "changeOrder should" - {
    "M: HasParent, parentId: Some, position: lowest, direction: inc" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      val wasMoved = db.run(
        dao.changeOrder(itemTable, i0, true)
      ).futureValue
      
      //then
      wasMoved should be(true)
      orderOfItem(i1) should be(0)
      orderOfItem(i0) should be(1)
      orderOfItem(i2) should be(2)
      orderOfItem(i3) should be(3)
    }
    "M: HasParent, parentId: Some, position: lowest, direction: dec" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      val wasMoved = db.run(
        dao.changeOrder(itemTable, i0, false)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfItem(i0) should be(0)
      orderOfItem(i1) should be(1)
      orderOfItem(i2) should be(2)
      orderOfItem(i3) should be(3)
    }
    "M: HasParent, parentId: Some, position: highest, direction: inc" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      val wasMoved = db.run(
        dao.changeOrder(itemTable, i3, true)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfItem(i0) should be(0)
      orderOfItem(i1) should be(1)
      orderOfItem(i2) should be(2)
      orderOfItem(i3) should be(3)
    }
    "M: HasParent, parentId: Some, position: highest, direction: dec" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      val wasMoved = db.run(
        dao.changeOrder(itemTable, i3, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfItem(i0) should be(0)
      orderOfItem(i1) should be(1)
      orderOfItem(i3) should be(2)
      orderOfItem(i2) should be(3)
    }
    "M: HasParent, parentId: Some, position: middle, direction: inc" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      val wasMoved = db.run(
        dao.changeOrder(itemTable, i1, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfItem(i0) should be(0)
      orderOfItem(i2) should be(1)
      orderOfItem(i1) should be(2)
      orderOfItem(i3) should be(3)
    }
    "M: HasParent, parentId: Some, position: middle, direction: dec" in {
      //given
      val (i0, i1, i2, i3) = createItems

      //when
      val wasMoved = db.run(
        dao.changeOrder(itemTable, i2, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfItem(i0) should be(0)
      orderOfItem(i2) should be(1)
      orderOfItem(i1) should be(2)
      orderOfItem(i3) should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: lowest, direction: inc" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d0.get, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d1) should be(0)
      orderOfDir(d0) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: lowest, direction: dec" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d0.get, false)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: highest, direction: inc" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d3.get, true)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: highest, direction: dec" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d3.get, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d3) should be(2)
      orderOfDir(d2) should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: middle, direction: inc" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d1.get, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d0) should be(0)
      orderOfDir(d2) should be(1)
      orderOfDir(d1) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: None, position: middle, direction: dec" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithoutParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d1.get, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d1) should be(0)
      orderOfDir(d0) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: lowest, direction: inc" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d0.get, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d1) should be(0)
      orderOfDir(d0) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: lowest, direction: dec" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d0.get, false)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: highest, direction: inc" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d3.get, true)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: highest, direction: dec" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d3.get, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d3) should be(2)
      orderOfDir(d2) should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: middle, direction: inc" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d2.get, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d0) should be(0)
      orderOfDir(d1) should be(1)
      orderOfDir(d3) should be(2)
      orderOfDir(d2) should be(3)
    }
    "M: HasOptionalParent, parentId: Some, position: middle, direction: dec" in {
      //given
      val (d0, d1, d2, d3) = createDirsWithParent

      //when
      val wasMoved = db.run(
        dao.changeOrder(directoryTable, d1.get, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfDir(d1) should be(0)
      orderOfDir(d0) should be(1)
      orderOfDir(d2) should be(2)
      orderOfDir(d3) should be(3)
    }
    "M: no-parent, parentId: None, position: lowest, direction: inc" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      val wasMoved = db.run(
        dao.changeOrder(taggTable, t0, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfTagg(t1) should be(0)
      orderOfTagg(t0) should be(1)
      orderOfTagg(t2) should be(2)
      orderOfTagg(t3) should be(3)
    }
    "M: no-parent, parentId: None, position: lowest, direction: dec" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      val wasMoved = db.run(
        dao.changeOrder(taggTable, t0, false)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfTagg(t0) should be(0)
      orderOfTagg(t1) should be(1)
      orderOfTagg(t2) should be(2)
      orderOfTagg(t3) should be(3)
    }
    "M: no-parent, parentId: None, position: highest, direction: inc" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      val wasMoved = db.run(
        dao.changeOrder(taggTable, t3, true)
      ).futureValue

      //then
      wasMoved should be(false)
      orderOfTagg(t0) should be(0)
      orderOfTagg(t1) should be(1)
      orderOfTagg(t2) should be(2)
      orderOfTagg(t3) should be(3)
    }
    "M: no-parent, parentId: None, position: highest, direction: dec" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      val wasMoved = db.run(
        dao.changeOrder(taggTable, t3, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfTagg(t0) should be(0)
      orderOfTagg(t1) should be(1)
      orderOfTagg(t3) should be(2)
      orderOfTagg(t2) should be(3)
    }
    "M: no-parent, parentId: None, position: middle, direction: inc" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      val wasMoved = db.run(
        dao.changeOrder(taggTable, t1, true)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfTagg(t0) should be(0)
      orderOfTagg(t2) should be(1)
      orderOfTagg(t1) should be(2)
      orderOfTagg(t3) should be(3)
    }
    "M: no-parent, parentId: None, position: middle, direction: dec" in {
      //given
      val (t0, t1, t2, t3) = createTaggs

      //when
      val wasMoved = db.run(
        dao.changeOrder(taggTable, t1, false)
      ).futureValue

      //then
      wasMoved should be(true)
      orderOfTagg(t1) should be(0)
      orderOfTagg(t0) should be(1)
      orderOfTagg(t2) should be(2)
      orderOfTagg(t3) should be(3)
    }
  }
}
