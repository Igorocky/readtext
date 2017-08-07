package db

import org.scalatest.BeforeAndAfter
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

trait DbTestHelperWithTables extends DbTestHelper with BeforeAndAfter {
  protected val tables: Seq[TableQuery[_ <: H2Profile.Table[_]]]

  override protected def beforeAll(): Unit = {
    db.run(
      DBIO.seq(tables.map(_.schema.create):_*)
    ).futureValue
  }

  before {
    db.run(
      DBIO.seq(tables.map(_.delete):_*).transactionally
    ).futureValue
    for {t <- tables} {
      db.run(t.length.result).futureValue should be(0)
    }
  }
}
