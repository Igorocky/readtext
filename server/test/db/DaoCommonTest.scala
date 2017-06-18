package db

import db.TestTables.directoryTable
import org.scalatest.BeforeAndAfter
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

class DaoCommonTest extends DbTestHelper with BeforeAndAfter {

  val tables = List(directoryTable)

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

  "insertOrdered" should "insert HasOptionalParent object with parentId == None into an empty table and set order = 0" in {
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
