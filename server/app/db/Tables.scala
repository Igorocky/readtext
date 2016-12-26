package db

import shared.pageparams.TextUI
import slick.driver.H2Driver.api._

class TextTable(tag: Tag) extends Table[TextUI](tag, "TEXTS") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def content = column[String]("content")
  def uniqTitle = index("uniq_title", (title), unique = true)
  def * = (id.?, title, content) <> (TextUI.tupled, TextUI.unapply)
}

trait Tables {
  val textTable = TableQuery[TextTable]
}