package db

import db.TestTables.{directoryTable, hiddenDirectoryTable, itemTable, taggTable}
import org.scalatest.BeforeAndAfter
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

trait DaoCommonTestHelper extends DbTestHelper with BeforeAndAfter {

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
}
