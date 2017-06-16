package controllers

import javax.inject._

import app.RouterBuilderUtils
import db.DaoCommon
import db.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.api.TopicApi
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class TopicApiImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             val dao: DaoCommon
                            )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with RouterBuilderUtils[TopicApi] {

  val router: Router = RouterBuilder()
    .addHandler(forMethod(_.loadTopicsByParentId)) {
      case parentId => db.run(
        dao.loadOrderedChildren(topicTable, parentId)
      ).map(_.toList)
    }
    .addHandler(forMethod(_.loadParagraphsByParentId)) {
      case optParentId => db.run(
        dao.loadOrderedChildren(paragraphTable, optParentId)
      ).map(_.toList)
    }
}
