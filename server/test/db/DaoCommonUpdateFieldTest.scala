package db

import db.TestTables.taggTable
import slick.jdbc.H2Profile.api._

class DaoCommonUpdateFieldTest extends DaoCommonTestHelper {

  def createTagg(order: Int) = db.run(
    taggTable returning taggTable.map(_.id) += Tagg(order = order, value = "t")
  ).futureValue

  def createTaggs = {
    (createTagg(0), createTagg(1))
  }

  def orderOfTagg(id: Long) = db.run(taggTable.filter(_.id === id).map(_.order).result.head).futureValue

  "updateField should update field by record id" in {
    //given
    val (t0, t1) = createTaggs

    //when
    db.run(
      dao.updateField(taggTable)(t1, _.order)(_ => 15)
    ).futureValue

    //then
    orderOfTagg(t0) should be(0)
    orderOfTagg(t1) should be(15)
  }

  "updateField should return new value" in {
    //given
    val (t0, t1) = createTaggs

    //when
    val newValue = db.run(
      dao.updateField(taggTable)(t1, _.order)(_ => 15)
    ).futureValue

    //then
    newValue should be(15)
  }
}
