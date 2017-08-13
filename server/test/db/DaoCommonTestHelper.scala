package db

import db.TestTables.{directoryTable, hiddenDirectoryTable, itemTable, taggTable}

import scala.concurrent.ExecutionContext.Implicits.global

trait DaoCommonTestHelper extends DbTestHelperWithTables {
  override protected val tables = List(directoryTable, itemTable, hiddenDirectoryTable, taggTable)

  val dao = new DaoCommon

  override def localBeforeEach: Unit = ()
}
