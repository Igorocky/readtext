package db

import javax.inject.{Inject, Singleton}

import slick.driver.H2Driver.api._
import slick.profile.RelationalProfile

import scala.concurrent.ExecutionContext
import scala.reflect.{ClassTag, classTag}

@Singleton
class DaoCommon @Inject()(implicit private val ec: ExecutionContext) {
  def changeOrder[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C], id: Long, down: Boolean) = {
    val hasParent = classOf[HasParent].isAssignableFrom(classTag[M].runtimeClass)
    val groupIdExtractor = createGroupIdExtractor(hasParent)
    val groupIdCriteria =createGroupIdCriteria(hasParent)
    (for {
      (groupId, currOrder) <- table.filter(_.id === id).map(r => (groupIdExtractor(r), r.order)).result.head
      ordersInsideGroup = table.filter(groupIdCriteria(groupId)).map(_.order)
      maxOrder <- maxFn(down, ordersInsideGroup).result.map(_.get)
      newOrder = calcNewOrder(down, currOrder)
      wasMoved <- if (currOrder == maxOrder) DBIO.successful(false) else for {
        _ <- table.filter(groupIdCriteria(groupId)).filter(_.order === newOrder).map(_.order).update(currOrder)
        _ <- table.filter(_.id === id).map(_.order).update(newOrder)
      } yield (true)
    } yield (wasMoved)).transactionally
  }

  def insertOrdered[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C])(parentId: Long, updateOrder: Int=>U, updateId: (U,Long)=>U) = {
    val hasParent = classOf[HasParent].isAssignableFrom(classTag[M].runtimeClass)
    val groupIdCriteria =createGroupIdCriteria(hasParent)
    (for {
      maxOrder <- table.filter(groupIdCriteria(parentId)).map(_.order).max.result.map(_.getOrElse(0))
      elemWithOrder = updateOrder(maxOrder + 1)
      newId <- table returning table.map(_.id) += elemWithOrder
    } yield (updateId(elemWithOrder, newId))).transactionally
  }

  def deleteOrdered[M <: HasIdAndOrder :ClassTag,U](table: Query[M,U,Seq], id: Long) = {
    val hasParent = classOf[HasParent].isAssignableFrom(classTag[M].runtimeClass)
    val groupIdExtractor = createGroupIdExtractor(hasParent)
    val groupIdCriteria =createGroupIdCriteria(hasParent)
    (for {
      (groupId, deletedOrder) <- table.filter(_.id === id).map(r => (groupIdExtractor(r), r.order)).result.head
      _ <- table.filter(_.id === id).asInstanceOf[Query[RelationalProfile#Table[_], _, Seq]].delete
      seq <- table.filter(groupIdCriteria(groupId)).filter(_.order > deletedOrder).map(t => (t.id,t.order)).result
      _ <- DBIO.sequence(for {
        (lowerElemId, lowerElemOrder) <- seq
      } yield table.filter(_.id === lowerElemId).map(_.order).update(lowerElemOrder - 1))
    } yield ()).transactionally
  }

  def updateField[M <: HasId, U, C[_], F: ColumnType](table: Query[M, U, C])(id: Long, extFunc: M => Rep[F])(mod: F => F) = {
    val selectionById = table.filter(_.id === id).map(extFunc)
    (for {
      oldValue <- selectionById.result.head
      newValue = mod(oldValue)
      _ <- selectionById.update(newValue)
    } yield (newValue)).transactionally
  }

  def loadOrderedChildren[M <: HasOptionalParent with HasOrder, U, C[_]](table: Query[M, U, C], parentId: Option[Long]) =
    table.filter(r => r.parentId === parentId || r.parentId.isEmpty && parentId.isEmpty).sortBy(_.order).result

  def loadOrderedChildren[M <: HasParent with HasOrder, U, C[_]](table: Query[M, U, C], parentId: Long) =
    table.filter(_.parentId === parentId).sortBy(_.order).result

  private def maxFn[C[_]](down: Boolean, q: Query[Rep[Int], Int, C]) = if (down) q.max else q.min
  private def calcNewOrder(down: Boolean, oldOrder: Int) = if (down) oldOrder + 1 else oldOrder - 1
  private def createGroupIdExtractor[M <: HasIdAndOrder](hasParent: Boolean): M => Rep[Long] =
    if (hasParent) _.asInstanceOf[HasParent].parentId
    else _ => (1L : Rep[Long])
  private def createGroupIdCriteria[M <: HasIdAndOrder](hasParent: Boolean): Rep[Long] => M => Rep[Boolean] =
    if (hasParent) groupId => row => row.asInstanceOf[HasParent].parentId === groupId
    else groupId => row => true: Rep[Boolean]
}
