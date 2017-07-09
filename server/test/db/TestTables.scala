package db

import slick.jdbc.H2Profile.api._

case class Directory(id: Option[Long] = None, parentId: Option[Long] = None, order: Int = -1, name: String)
case class Item(id: Option[Long] = None, directoryId: Long, order: Int = -1, value: String)

class DirectoryTable(tag: Tag) extends Table[Directory](tag, "DIRECTORIES") with HasIdAndOrder with HasOptionalParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  override def parentId = column[Option[Long]]("parentId")
  override def order = column[Int]("order")
  def name = column[String]("name")

  def parentFolder = foreignKey("PARENT_FOLDER_FK", parentId, TestTables.directoryTable)(_.id.?, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, parentId, order, name) <> (Directory.tupled, Directory.unapply)
}

class ItemTable(tag: Tag) extends Table[Item](tag, "ITEMS") with HasIdAndOrder with HasParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def directoryId = column[Long]("directoryId")
  override def order = column[Int]("order")
  def value = column[String]("value")

  override def parentId = directoryId

  def parentFolder = foreignKey("ITEMS_PARENT_FOLDER_FK", directoryId, TestTables.directoryTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, directoryId, order, value) <> (Item.tupled, Item.unapply)
}

object TestTables {
  val directoryTable = TableQuery[DirectoryTable]
  val itemTable = TableQuery[ItemTable]
}