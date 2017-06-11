package db

import java.sql.Timestamp
import java.time.{ZoneOffset, ZonedDateTime}

import shared.StrUtils
import shared.dto._
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

trait HasOptionalParent {
  def parentId: Rep[Option[Long]]
}

object TypeConversions {
  implicit val listOfStringsColumnType = MappedColumnType.base[List[String], String](
    StrUtils.listToStr,
    StrUtils.strToList
  )
  implicit val cardTypeColumnType = MappedColumnType.base[CardType, Int](
    cardType => cardType.id,
    id => CardTypes.allElems.find(_.id == id).get
  )
  implicit val zdtColumnType = MappedColumnType.base[ZonedDateTime, Timestamp](
//    zdt => Timestamp.from(zdt.toInstant()),
    zdt => {
//      val utcZdt = zdt.withZoneSameInstant(ZoneOffset.UTC)
      /*new Timestamp(utcZdt.getYear, utcZdt.getMonth.getValue, utcZdt.getDayOfMonth,
        utcZdt.getHour, utcZdt.getMinute, utcZdt.getSecond, utcZdt.getNano)*/
      new Timestamp(zdt.withZoneSameInstant(ZoneOffset.UTC).toEpochSecond)
    },
    ts => ZonedDateTime.ofInstant(ts.toInstant, ZoneOffset.UTC)
  )
}

class ParagraphTable(tag: Tag) extends Table[Paragraph](tag, "PARAGRAPHS") with HasIdAndOrder with HasOptionalParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def paragraphId = column[Option[Long]]("paragraphId")
  def name = column[String]("name")
  def expanded = column[Boolean]("expanded")
  override def order = column[Int]("order")

  def paragraph = foreignKey("PAR_PARAGRAPH_FK", paragraphId, Tables.paragraphTable)(_.id.?, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  override def parentId = paragraphId

  def * = (id.?, paragraphId, name, expanded, order) <> (Paragraph.tupled, Paragraph.unapply)
}

class TopicTable(tag: Tag) extends Table[Topic](tag, "TOPICS") with HasIdAndOrder with HasParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def paragraphId = column[Long]("paragraphId")
  def title = column[String]("title")
  override def order = column[Int]("order")
  def images = column[List[String]]("images")
  def tags = column[List[String]]("tags", O.Default(Nil))

  def paragraph = foreignKey("PARAGRAPH_FK", paragraphId, Tables.paragraphTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  override def parentId = paragraphId

  def * = (id.?, paragraphId.?, title,  order, images, tags) <> (Topic.tupled, Topic.unapply)
}

class FolderTable(tag: Tag) extends Table[Folder](tag, "FOLDERS") with HasIdAndOrder with HasParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def parentFolderId = column[Long]("parentFolderId")
  def name = column[String]("name")
  override def order = column[Int]("order")
  def created = column[ZonedDateTime]("created")

  def parentFolder = foreignKey("PARENT_FOLDER_FK", parentFolderId, Tables.folderTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, parentFolderId.?, name, order, created) <> (Folder.tupled, Folder.unapply)

  override def parentId = parentFolderId
}

class CardTable(tag: Tag) extends Table[Card](tag, "CARDS") with HasIdAndOrder with HasParent {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def cardType = column[CardType]("cardType")
  def folderId = column[Long]("folderId")
  def created = column[ZonedDateTime]("created")
  def questionId = column[Long]("questionId")
  def answerId = column[Long]("answerId")
  override def order = column[Int]("order")

  def folder = foreignKey("FOLDER_FK", folderId, Tables.folderTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, cardType, folderId, created, questionId, answerId, order) <> (Card.tupled, Card.unapply)

  override def parentId = folderId
}

object Tables {
  val paragraphTable = TableQuery[ParagraphTable]
  val topicTable = TableQuery[TopicTable]
  val folderTable = TableQuery[FolderTable]
  val cardTable = TableQuery[CardTable]
}