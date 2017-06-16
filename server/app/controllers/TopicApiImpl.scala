package controllers

import javax.inject._

import db.DaoCommon
import db.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.dto.{Paragraph, Topic}
import slick.driver.JdbcProfile
import upickle.default._

import scala.concurrent.ExecutionContext

@Singleton
class TopicApiImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             val dao: DaoCommon
                            )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  val router = RouterBuilder()
    .addHandler(path = "shared.api.TopicApi.loadTopicsByParentId", reader = read[Long], writer = write(_: List[Topic])) {
      case parentId => db.run(
        dao.loadOrderedChildren(topicTable, parentId)
      ).map(_.toList)
    }
    .addHandler(path = "shared.api.TopicApi.loadParagraphsByParentId", reader = read[Option[Long]], writer = write(_: List[Paragraph])) {
      case optParentId => db.run(
        dao.loadOrderedChildren(paragraphTable, optParentId)
      ).map(_.toList)
    }
}
