package db

import javax.inject.{Inject, Singleton}

import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext
import scala.reflect.{ClassTag, classTag}

@Singleton
class Dao @Inject()(implicit private val ec: ExecutionContext) {
  private def maxFn[C[_]](down: Boolean, q: Query[Rep[Int], Int, C]) = if (down) q.max else q.min
  private def calcNewOrder(down: Boolean, oldOrder: Int) = if (down) oldOrder + 1 else oldOrder - 1

  def changeOrder[M <: HasIdAndOrder :ClassTag,U,C[_]](id: Long, table: Query[M,U,C], down: Boolean) = {
    val (groupIdExtractor, groupIdCriteria): (M => Rep[Long], Rep[Long] => M => Rep[Boolean]) =
      if (classOf[HasParent].isAssignableFrom(classTag[M].runtimeClass))
        (_.asInstanceOf[HasParent].parentId, groupId => row => row.asInstanceOf[HasParent].parentId === groupId)
      else
        (_ => (1L : Rep[Long]), groupId => row => true: Rep[Boolean])
    (for {
      (groupId, currOrder) <- table.filter(_.id === id).map(r => (groupIdExtractor(r), r.order)).result.head
      ordersInsideGroup = table.filter(groupIdCriteria(groupId)).map(_.order)
      maxOrder <- maxFn(down, ordersInsideGroup).result.map(_.get)
      newOrder = calcNewOrder(down, currOrder)
      _ <- if (currOrder == maxOrder) DBIO.successful(()) else for {
        _ <- table.filter(groupIdCriteria(groupId)).filter(_.order === newOrder).map(_.order).update(currOrder)
        _ <- table.filter(_.id === id).map(_.order).update(newOrder)
      } yield ()
    } yield ()).transactionally
  }
}
