package db

import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext

class PrintSchema(implicit private val ec: ExecutionContext) {
  import Tables._

  val tables = List(paragraphTable,topicTable,folderTable,cardTable)

  println("===========================")
  tables.foreach(_.schema.create.statements.map(">>>" + _).foreach(println))
  println("###########################")
  tables.foreach(_.schema.drop.statements.map(">>>" + _).foreach(println))
  println("===========================")

}
