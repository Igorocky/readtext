package db

import shared.dto.{Paragraph, Topic}
import slick.driver.H2Driver.api._

trait HasId {
  def id: Rep[Long]
}

trait HasOrder {
  def order: Rep[Int]
}

trait HasIdAndOrder extends HasId with HasOrder

object TypeConversions {
  implicit val listOfStringsColumnType = MappedColumnType.base[List[String], String](
    _.mkString(";"),
    str => if (str == null || str.trim == "") Nil else str.split(";").toList
  )
}

class ParagraphTable(tag: Tag) extends Table[Paragraph](tag, "PARAGRAPHS") with HasIdAndOrder {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def checked = column[Boolean]("checked")
  def name = column[String]("name")
  def expanded = column[Boolean]("expanded")
  def order = column[Int]("order")

  def * = (id.?, checked, name, expanded, order) <> (
    (t: (Option[Long], Boolean, String, Boolean, Int)) =>
      Paragraph(t._1, t._2, t._3, t._4, t._5, Nil),
    (p: Paragraph) => Some((p.id, p.checked, p.name, p.expanded, p.order))
  )
}

class TopicTable(tag: Tag) extends Table[Topic](tag, "TOPICS") with HasIdAndOrder {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def paragraphId = column[Long]("paragraphId")
  def checked = column[Boolean]("checked")
  def title = column[String]("title")
  def order = column[Int]("order")
  def images = column[String]("images")
  def tags = column[String]("tags", O.Default(""))

  def paragraph = foreignKey("PARAGRAPH_FK", paragraphId, Tables.paragraphTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?,         paragraphId.?, checked, title,  order, images, tags) <> (
    (t: (  Option[Long], Option[Long],  Boolean, String, Int,   String, String)) =>
      Topic(t._1, t._2, t._3, t._4, t._5).setImages(t._6).setTags(t._7),
    (t: Topic) => Some((t.id, t.paragraphId, t.checked, t.title, t.order, t.imagesStr, t.tagsStr))
  )
}

object Tables {
  val paragraphTable = TableQuery[ParagraphTable]
  val topicTable = TableQuery[TopicTable]
}