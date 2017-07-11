package db

import db.TestTables.{directoryTable, hiddenDirectoryTable, itemTable, taggTable}
import org.scalatest.BeforeAndAfter
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class DaoCommonTest extends DbTestHelper with BeforeAndAfter {

  val tables = List(directoryTable, itemTable, hiddenDirectoryTable, taggTable)

  override protected def beforeAll(): Unit = {
    db.run(
      DBIO.seq(tables.map(_.schema.create):_*)
    )
  }

  before {
    db.run(
      DBIO.seq(tables.map(_.delete):_*)
    )
    for {t <- tables} {
      db.run(t.length.result).futureValue should be(0)
    }
  }

  val dao = new DaoCommon

  "insertOrdered should" - {
    "M: HasParent, table: empty, parentId: Some, siblings: no" - {
      "set order == 0" in {
        //given
        val dirId = db.run(
          directoryTable returning directoryTable.map(_.id) += Directory(parentId = None, order = 0, name = "d1")
        ).futureValue
        val item = Item(directoryId = dirId, value = "i0")

        //when
        val itemId = db.run(
          dao.insertOrdered(itemTable, item.directoryId)(
            o => item.copy(order = o),
            (item,id) => item.copy(id = Some(id))
          )
        ).futureValue.id.get

        //then
        db.run(itemTable.filter(_.id === itemId).map(_.order).result.head).futureValue should be(0)
      }
    }

    "M: HasParent, table: non-empty, parentId: Some, siblings: yes" - {
      "set order == 'number of siblings'" in {
        //given
        val dirId = db.run(for {
          dirId <- directoryTable returning directoryTable.map(_.id) += Directory(parentId = None, order = 0, name = "d1")
          _ <- itemTable ++= List(
            Item(id = Some(1), directoryId = dirId, order = 0, value = "i1")
            ,Item(id = Some(2), directoryId = dirId, order = 1, value = "i2")
            ,Item(id = Some(3), directoryId = dirId, order = 2, value = "i3")
          )
        } yield dirId).futureValue
        val item = Item(directoryId = dirId, value = "i4")

        //when
        val itemId = db.run(
          dao.insertOrdered(itemTable, item.directoryId)(
            o => item.copy(order = o),
            (item,id) => item.copy(id = Some(id))
          )
        ).futureValue.id.get

        //then
        db.run(itemTable.filter(_.id === itemId).map(_.order).result.head).futureValue should be(3)
      }
    }
    "M: HasParent, table: non-empty, parentId: Some, siblings: no" - {
      "set order == 0" in {
        //given
        val dirId = db.run(for {
          dirId1 <- directoryTable returning directoryTable.map(_.id) += Directory(parentId = None, order = 0, name = "d0")
          dirId2 <- directoryTable returning directoryTable.map(_.id) += Directory(parentId = None, order = 1, name = "d1")
          _ <- itemTable ++= List(
            Item(id = Some(1), directoryId = dirId1, order = 0, value = "i1")
            ,Item(id = Some(2), directoryId = dirId1, order = 1, value = "i2")
            ,Item(id = Some(3), directoryId = dirId1, order = 2, value = "i3")
          )
        } yield dirId2).futureValue
        val item = Item(directoryId = dirId, value = "i4")

        //when
        val itemId = db.run(
          dao.insertOrdered(itemTable, item.directoryId)(
            o => item.copy(order = o),
            (item,id) => item.copy(id = Some(id))
          )
        ).futureValue.id.get

        //then
        db.run(itemTable.filter(_.id === itemId).map(_.order).result.head).futureValue should be(0)
      }
    }
    "M: HasOptionalParent, table: empty, parentId: None, siblings: no" - {
      "set order == 0" in {
        //given
        val dir1 = Directory(name = "dir1")

        //when
        val dirId = db.run(
          dao.insertOrdered(directoryTable, dir1.parentId)(
            o => dir1.copy(order = o),
            (dir,id) => dir.copy(id = Some(id))
          )
        ).futureValue.id.get

        //then
        db.run(directoryTable.filter(_.id === dirId).map(_.order).result.head).futureValue should be(0)
      }
    }
    "M: HasOptionalParent, table: empty, parentId: Some, siblings: no" - {
      "set order == 0" in {
        //given
        val parentDirId = db.run(
          directoryTable returning directoryTable.map(_.id) += Directory(name = "parentDir")
        ).futureValue

        //when
        val hiddenDir = db.run(
          dao.insertOrdered(hiddenDirectoryTable, parentDirId)(
            o => Directory(name = "hidden-dir", order = o),
            (dir, id) => dir.copy(id = Some(id))
          )
        ).futureValue

        //then
        hiddenDir.order should be(0)
      }
    }
    "M: HasOptionalParent, table: non-empty, parentId: None, siblings: yes" - {
      "set order == 'number of siblings'" in {
        //given
        db.run(
          directoryTable ++= List(
            Directory(name = "d1", order = 0)
            ,Directory(name = "d2", order = 1)
            ,Directory(name = "d3", order = 2)
          )
        ).futureValue

        //when
        val dir = db.run(
          dao.insertOrdered(directoryTable, None)(
            o => Directory(name = "d4", order = o),
            (dir, id) => dir.copy(id = Some(id))
          )
        ).futureValue

        //then
        dir.order should be(3)
      }
    }
    "M: HasOptionalParent, table: non-empty, parentId: None, siblings: no" - {
      "set order == 0" in {
        //given
        val normalDirId = db.run(
          directoryTable returning directoryTable.map(_.id) += Directory(name = "d1")
        ).futureValue

        db.run(
          hiddenDirectoryTable ++= List(
            Directory(name = "hd1", order = 0, parentId = Some(normalDirId))
            ,Directory(name = "hd2", order = 1, parentId = Some(normalDirId))
          )
        ).futureValue

        db.run(hiddenDirectoryTable.length.result).futureValue should be(2)

        //when
        val dir = db.run(
          dao.insertOrdered(hiddenDirectoryTable, None)(
            o => Directory(name = "hd3", order = o),
            (dir, id) => dir.copy(id = Some(id))
          )
        ).futureValue

        //then
        dir.order should be(0)
      }
    }
    "M: HasOptionalParent, table: non-empty, parentId: Some, siblings: yes" - {
      "set order == 'number of siblings'" in {
        //given
        val rootDirId = db.run(
          directoryTable returning directoryTable.map(_.id) += Directory(name = "root")
        ).futureValue

        db.run(
          directoryTable ++= List(
            Directory(name = "d1", order = 0, parentId = Some(rootDirId))
            ,Directory(name = "d2", order = 1, parentId = Some(rootDirId))
          )
        ).futureValue

        db.run(directoryTable.length.result).futureValue should be(3)

        //when
        val dir = db.run(
          dao.insertOrdered(directoryTable, Some(rootDirId))(
            o => Directory(name = "d3", order = o),
            (dir, id) => dir.copy(id = Some(id))
          )
        ).futureValue

        //then
        dir.order should be(2)
      }
    }
    "M: HasOptionalParent, table: non-empty, parentId: Some, siblings: no" - {
      "set order == 0" in {
        //given
        val rootDirId = db.run(
          directoryTable returning directoryTable.map(_.id) += Directory(name = "root")
        ).futureValue

        db.run(directoryTable.length.result).futureValue should be(1)

        //when
        val dir = db.run(
          dao.insertOrdered(directoryTable, Some(rootDirId))(
            o => Directory(name = "d3", order = o),
            (dir, id) => dir.copy(id = Some(id))
          )
        ).futureValue

        //then
        dir.order should be(0)
      }
    }
    "M: no-parent, table: empty, parentId: None, siblings: no" - {
      "set order == 0" in {
        //given
        db.run(taggTable.size.result).futureValue should be(0)

        //when
        val tag = db.run(
          dao.insertOrdered(taggTable, None)(
            o => Tagg(value = "TTT", order = o),
            (tag, id) => tag.copy(id = Some(id))
          )
        ).futureValue

        //then
        tag.order should be(0)
      }
    }
    "M: no-parent, table: non-empty, parentId: None, siblings: yes" - {
      "set order == 'number of siblings'" in {
        //given
        db.run(
          taggTable ++= List(
            Tagg(value = "t1", order = 0)
            ,Tagg(value = "t2", order = 1)
            ,Tagg(value = "t3", order = 2)
          )
        )
        db.run(taggTable.size.result).futureValue should be(3)

        //when
        val tag = db.run(
          dao.insertOrdered(taggTable, None)(
            o => Tagg(value = "TTT", order = o),
            (tag, id) => tag.copy(id = Some(id))
          )
        ).futureValue

        //then
        tag.order should be(3)
      }
    }
  }

}
