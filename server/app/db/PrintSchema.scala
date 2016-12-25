package db

import slick.driver.H2Driver.api._

class PrintSchema extends Tables {
  textTable.schema.create.statements.map(">>>" + _).foreach(println)
}
