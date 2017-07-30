package db

import javax.inject.{Inject, Singleton}

import slick.jdbc.H2Profile.api._
import slick.relational.RelationalProfile

import scala.concurrent.ExecutionContext
import scala.reflect.{ClassTag, classTag}

@Singleton
class DaoCommon @Inject()(implicit private val ec: ExecutionContext) {
  def changeOrder[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C], id: Long, orderShouldBeIncreased: Boolean) = {
    val parentIdExtractor = createParentIdExtractor[M]
    (for {
      (parentId, currOrder) <- table.filter(_.id === id).map(r => (parentIdExtractor(r), r.order)).result.head
      haveSameParent = createHaveSameParentCriteria(parentId)
      ordersInsideGroup = table.filter(haveSameParent).map(_.order)
      edgeOrder <- findEdge(ordersInsideGroup, orderShouldBeIncreased).result.map(_.get)
      newOrder = calcNewOrder(currOrder, orderShouldBeIncreased)
      wasMoved <- if (currOrder == edgeOrder) DBIO.successful(false) else for {
        _ <- table.filter(haveSameParent).filter(_.order === newOrder).map(_.order).update(currOrder)
        _ <- table.filter(_.id === id).map(_.order).update(newOrder)
      } yield (true)
    } yield (wasMoved)).transactionally
  }

  def insertOrdered[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C], parentId: Long)
                                                        (updateOrder: Int=>U, updateId: (U,Long)=>U): DBIOAction[U, NoStream, Effect.Read with Effect.Write with Effect.Transactional] =
    insertOrdered(table, Some(parentId))(updateOrder, updateId)

  def insertOrdered[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C], parentId: Option[Long])
                                                        (updateOrder: Int=>U, updateId: (U,Long)=>U): DBIOAction[U, NoStream, Effect.Read with Effect.Write with Effect.Transactional] = {
    val haveSameParent = createHaveSameParentCriteria(parentId)
    (for {
      maxOrder <- table.filter(haveSameParent).map(_.order).max.result.map(_.getOrElse(-1))
      elemWithOrder = updateOrder(maxOrder + 1)
      newId <- table returning table.map(_.id) += elemWithOrder
    } yield (updateId(elemWithOrder, newId))).transactionally
  }

  def changeParentOrdered[M <: HasIdAndOrder :ClassTag,U](table: Query[M,U,Seq], id: Long, newParentId: Long): DBIOAction[Int, NoStream, Effect.Read with Effect.Read with Effect.Write with Effect.Read with Effect.Write with Effect.Transactional] =
    changeParentOrdered(table, id, Some(newParentId))

  def changeParentOrdered[M <: HasIdAndOrder :ClassTag,U](table: Query[M,U,Seq], id: Long, newParentId: Option[Long]): DBIOAction[Int, NoStream, Effect.Read with Effect.Read with Effect.Write with Effect.Read with Effect.Write with Effect.Transactional] = {
    val parentIdExtractor = createParentIdExtractor[M]
    val selectParentIdAndOrder = table.filter(_.id === id).map(r => (parentIdExtractor(r), r.order))
    (for {
      (parentId, deletedOrder) <- selectParentIdAndOrder.result.head
      haveSameParent = createHaveSameParentCriteria(parentId)
      seq <- table.filter(haveSameParent).filter(_.order > deletedOrder).map(t => (t.id,t.order)).result
      _ <- DBIO.sequence(
        for {
          (lowerElemId, lowerElemOrder) <- seq
        } yield table.filter(_.id === lowerElemId).map(_.order).update(lowerElemOrder - 1)
      )
      haveSameNewParent = createHaveSameParentCriteria(newParentId)
      maxNewOrder <- table.filter(haveSameNewParent).map(_.order).max.result.map(_.getOrElse(-1))
      newOrder = maxNewOrder + 1
      _ <- selectParentIdAndOrder.update((newParentId, newOrder))
    } yield newOrder).transactionally

  }

  def deleteOrdered[M <: HasIdAndOrder :ClassTag,U](table: Query[M,U,Seq], id: Long) = {
    val parentIdExtractor = createParentIdExtractor[M]
    (for {
      (parentId, deletedOrder) <- table.filter(_.id === id).map(r => (parentIdExtractor(r), r.order)).result.head
      _ <- table.filter(_.id === id).asInstanceOf[Query[RelationalProfile#Table[_], _, Seq]].delete
      haveSameParent = createHaveSameParentCriteria(parentId)
      seq <- table.filter(haveSameParent).filter(_.order > deletedOrder).map(t => (t.id,t.order)).result
      _ <- DBIO.sequence(
        for {
          (lowerElemId, lowerElemOrder) <- seq
        } yield table.filter(_.id === lowerElemId).map(_.order).update(lowerElemOrder - 1)
      )
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

  private def findEdge[C[_]](q: Query[Rep[Int], Int, C], edgeIsMax: Boolean) = if (edgeIsMax) q.max else q.min
  private def calcNewOrder(oldOrder: Int, edgeIsMax: Boolean) = if (edgeIsMax) oldOrder + 1 else oldOrder - 1

  private def createParentIdExtractor[M :ClassTag]: M => Rep[Option[Long]] =
    if (classOf[HasParent].isAssignableFrom(classTag[M].runtimeClass))
      _.asInstanceOf[HasParent].parentId.?
    else if (classOf[HasOptionalParent].isAssignableFrom(classTag[M].runtimeClass))
      _.asInstanceOf[HasOptionalParent].parentId
    else
      _ => (None : Rep[Option[Long]])

  private def createHaveSameParentCriteria[M :ClassTag](parentId: Long): M => Rep[Option[Boolean]] =
    createHaveSameParentCriteria(Some(parentId))

  private def createHaveSameParentCriteria[M :ClassTag](parentId: Option[Long]): M => Rep[Option[Boolean]] =
    if (classOf[HasParent].isAssignableFrom(classTag[M].runtimeClass))
      row => row.asInstanceOf[HasParent].parentId.? === parentId.get
    else if (classOf[HasOptionalParent].isAssignableFrom(classTag[M].runtimeClass))
      if (parentId.isEmpty)
        row => row.asInstanceOf[HasOptionalParent].parentId.isEmpty.?
      else
        row => row.asInstanceOf[HasOptionalParent].parentId === parentId
    else
      _ => (true: Rep[Boolean]).?
}
