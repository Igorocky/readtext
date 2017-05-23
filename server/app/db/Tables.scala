package db

import shared.StrUtils
import shared.dto.{Paragraph, Topic}
import slick.driver.H2Driver.api._
import TypeConversions._

trait HasId {
  def id: Rep[Long]
}

trait HasOrder {
  def order: Rep[Int]
}

trait HasIdAndOrder extends HasId with HasOrder

trait HasParent {
  def parentId: Rep[Long]
}

object TypeConversions {
  implicit val listOfStringsColumnType = MappedColumnType.base[List[String], String](
    StrUtils.listToStr,
    StrUtils.strToList
  )
}

class ParagraphTable(tag: Tag) extends Table[Paragraph](tag, "PARAGRAPHS") with HasIdAndOrder {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def checked = column[Boolean]("checked")
  def name = column[String]("name")
  def expanded = column[Boolean]("expanded")
  override def order = column[Int]("order")

  def * = (id.?, checked, name, expanded, order) <> (
    (t: (Option[Long], Boolean, String, Boolean, Int)) =>
      Paragraph(t._1, t._2, t._3, t._4, t._5, Nil),
    (p: Paragraph) => Some((p.id, p.checked, p.name, p.expanded, p.order))
  )
}

class TopicTable(tag: Tag) extends Table[Topic](tag, "TOPICS") with HasIdAndOrder with HasParent{
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def paragraphId = column[Long]("paragraphId")
  def checked = column[Boolean]("checked")
  def title = column[String]("title")
  override def order = column[Int]("order")
  def images = column[List[String]]("images")
  def tags = column[List[String]]("tags", O.Default(Nil))

  def paragraph = foreignKey("PARAGRAPH_FK", paragraphId, Tables.paragraphTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, paragraphId.?, checked, title,  order, images, tags) <> (Topic.tupled, Topic.unapply)

  override def parentId = paragraphId
}

object Tables {
  val paragraphTable = TableQuery[ParagraphTable]
  val topicTable = TableQuery[TopicTable]
}