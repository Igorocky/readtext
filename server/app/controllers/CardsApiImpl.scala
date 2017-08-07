package controllers

import javax.inject._

import app.RouterBuilderUtils
import db.DaoCommon
import db.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.api.CardsApi
import shared.dto._
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CardsApiImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             val dao: DaoCommon
                            )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with RouterBuilderUtils[CardsApi] {

  var questionCnt = 0

  val router: Router = RouterBuilder()
    // TODO: use timezone of the user
    .addHandler(forMethod(_.loadCardStates)) {
      case poolId => for {
        topics <- loadTopicsRecursively(poolId)
      } yield topics.map{
        case (topic, None) => TopicState(topic.id.get, EasinessLevels.EASY, ScoreLevels.EXCELLENT, None)
        case (topic, Some(r)) => TopicState(topic.id.get, r.easiness, r.score, Some(r.time.toString))
      }
    }

    .addHandler(forMethod3(_.updateCardState)) {
      case (cardId, easiness, score) => updateTopicState(cardId, easiness, score)
    }

    .addHandler(forMethod(_.loadTopic)) {
      case topicId => db.run(
        topicTable.filter(_.id === topicId).result.head
      )
    }

  protected[controllers] def updateTopicState(topicId: Long, easiness: Easiness, score: Score): Future[Unit] = {
    val historyRecord = TopicHistoryRecord(topicId = topicId, easiness = easiness, score = score)
    db.run(DBIO.seq(
      topicHistoryTable += historyRecord,
      topicStateTable.insertOrUpdate(historyRecord)
    ).transactionally)
  }

  protected[controllers] def loadParagraphsRecursively(parentParagraphIds: List[Long]): Future[List[Long]] = for {
      childParagraphs <- db.run(paragraphTable.filter(_.parentId inSet(parentParagraphIds)).map(_.id).result).map(_.toList)
      rest <- if (childParagraphs.isEmpty) Future.successful(Nil) else loadParagraphsRecursively(childParagraphs)
    } yield parentParagraphIds:::rest

  protected[controllers] def loadTopicsRecursively(paragraphId: Long): Future[List[(Topic, Option[TopicHistoryRecord])]] = {
    for {
      paragraphIds <- loadParagraphsRecursively(List(paragraphId))
      res <- db.run(
        topicTable.filter(_.paragraphId inSet(paragraphIds))
          .joinLeft(topicStateTable).on(_.id === _.topicId)
          .result
      )
    } yield res.sortBy(_._2.map(_.time.toEpochSecond).getOrElse(0L))
  }.map(_.toList)
}
