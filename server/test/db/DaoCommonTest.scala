package db

import db.TestTables.{directoryTable, itemTable}
import org.scalatest.BeforeAndAfter
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class DaoCommonTest extends DbTestHelper with BeforeAndAfter {

  val tables = List(directoryTable, itemTable)

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

  "M: HasParent" - {
    "table: empty" - {
      "parentId: Some" - {
        "siblings: no" - {
          "insertOrdered should set order == 0" in {
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
      }
    }

    "table: non-empty" - {
      "parentId: Some" - {
        "siblings: yes" - {
          "insertOrdered should set order == 'number of siblings'" in {
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

        "siblings: no" - {
          "insertOrdered should set order == 0" in {
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
      }
    }
  }

  "M: HasOptionalParent" - {
    "table: empty" - {
      "parentId: None" - {
        "siblings: no" - {
          "insertOrdered should set order == 0" in {
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
      }

      "parentId: Some" - {
        "siblings: no" - {
          "insertOrdered should set order == 0" in {}
        }
      }
    }
  }

}
