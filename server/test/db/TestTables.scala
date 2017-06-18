package db

import slick.jdbc.H2Profile.api._

case class Directory(id: Option[Long] = None, parentId: Option[Long] = None, order: Int = -1, name: String)

class DirectoryTable(tag: Tag) extends Table[Directory](tag, "DIRECTORIES") with HasIdAndOrder with HasOptionalParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  override def parentId = column[Option[Long]]("parentId")
  override def order = column[Int]("order")
  def name = column[String]("name")

  def parentFolder = foreignKey("PARENT_FOLDER_FK", parentId, TestTables.directoryTable)(_.id.?, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, parentId, order, name) <> (Directory.tupled, Directory.unapply)
}

object TestTables {
  val directoryTable = TableQuery[DirectoryTable]
}