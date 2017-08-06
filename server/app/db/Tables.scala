package db

import java.sql.Timestamp
import java.time.{ZoneOffset, ZonedDateTime}

import shared.StrUtils
import shared.dto._
import slick.jdbc.H2Profile.api._
import TypeConversions._

import upickle.default._

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
  implicit val mapOfStringsColumnType = MappedColumnType.base[Map[String, String], String](
    write[Map[String, String]](_),
    read[Map[String, String]]
  )
  implicit val questionTypeColumnType = MappedColumnType.base[QuestionType, Int](
    cardType => cardType.id,
    id => QuestionTypes.allElems.find(_.id == id).get
  )
  implicit val answerTypeColumnType = MappedColumnType.base[AnswerType, Int](
    answerType => answerType.id,
    id => AnswerTypes.allElems.find(_.id == id).get
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

  def * = (id.?, paragraphId, title,  order, images, tags) <> (Topic.tupled, Topic.unapply)
}

class FolderTable(tag: Tag) extends Table[Folder](tag, "FOLDERS") with HasIdAndOrder with HasOptionalParent {
  override def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def sourceId = column[String]("sourceId")
  def parentFolderId = column[Option[Long]]("parentFolderId")
  def name = column[String]("name")
  override def order = column[Int]("order")
  def created = column[ZonedDateTime]("created")
  def modified = column[ZonedDateTime]("modified")

  def parentFolder = foreignKey("PARENT_FOLDER_FK", parentFolderId, Tables.folderTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, sourceId, parentFolderId, name, order, created, modified) <> (Folder.tupled, Folder.unapply)

  override def parentId = parentFolderId
}

class CardTable(tag: Tag) extends Table[Card](tag, "CARDS") with HasIdAndOrder with HasParent {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def sourceId = column[String]("sourceId")
  def folderId = column[Long]("folderId")
  def questionType = column[QuestionType]("questionType")
  def answerType = column[AnswerType]("answerType")
  def created = column[ZonedDateTime]("created")
  def modified = column[ZonedDateTime]("modified")
  override def order = column[Int]("order")

  def folder = foreignKey("FOLDER_FK", folderId, Tables.folderTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, sourceId, folderId, questionType, answerType, created, modified, order) <> (Card.tupled, Card.unapply)

  override def parentId = folderId
}

class TextQATable(tag: Tag) extends Table[TextQA](tag, "TEXT_QUESTIONS") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def cardId = column[Long]("cardId")
  def text = column[String]("text")
  def created = column[ZonedDateTime]("created")
  def modified = column[ZonedDateTime]("modified")

  def card = foreignKey("TEXT_QA_CARD_FK", cardId, Tables.cardTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, cardId, text, created, modified) <> (TextQA.tupled, TextQA.unapply)
}

class ImageQATable(tag: Tag) extends Table[ImageQA](tag, "TOPIC_ANSWERS") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def cardId = column[Long]("cardId")
  def imagePaths = column[List[String]]("imagePaths")
  def created = column[ZonedDateTime]("created")
  def modified = column[ZonedDateTime]("modified")

  def card = foreignKey("IMAGE_QA_CARD_FK", cardId, Tables.cardTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def * = (id.?, cardId, imagePaths, created, modified) <> (ImageQA.tupled, ImageQA.unapply)
}



object Tables {
  val paragraphTable = TableQuery[ParagraphTable]
  val topicTable = TableQuery[TopicTable]
  val folderTable = TableQuery[FolderTable]
  val cardTable = TableQuery[CardTable]
  val textQATable = TableQuery[TextQATable]
  val topicQATable = TableQuery[ImageQATable]
}