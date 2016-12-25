package db

import slick.driver.H2Driver.api._

case class Text(title: String, content: String)
class TextTable(tag: Tag) extends Table[Text](tag, "TEXTS") {
  def title = column[String]("title", O.PrimaryKey)
  def content = column[String]("content")
  def * = (title, content) <> (Text.tupled, Text.unapply)
}

trait Tables {
  val textTable = TableQuery[TextTable]
}