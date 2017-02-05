package db

import slick.driver.H2Driver.api._

class PrintSchema {
  import Tables._
  println("===========================")
  textTable.schema.create.statements.map(">>>" + _).foreach(println)
  println("===========================")
  paragraphTable.schema.create.statements.map(">>>" + _).foreach(println)
  println("===========================")
  topicTable.schema.create.statements.map(">>>" + _).foreach(println)
  println("===========================")
  println("###########################")
  println("===========================")
  textTable.schema.drop.statements.map(">>>" + _).foreach(println)
  println("===========================")
  paragraphTable.schema.drop.statements.map(">>>" + _).foreach(println)
  println("===========================")
  topicTable.schema.drop.statements.map(">>>" + _).foreach(println)
  println("===========================")
}
