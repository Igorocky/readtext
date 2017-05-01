package db

import slick.driver.H2Driver.api._

class PrintSchema {
  import Tables._

  val tables = List(paragraphTable,topicTable)

  println("===========================")
  tables.foreach(_.schema.create.statements.map(">>>" + _).foreach(println))
  println("###########################")
  tables.foreach(_.schema.drop.statements.map(">>>" + _).foreach(println))
  println("===========================")
}
