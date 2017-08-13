package controllers

import java.time.{Duration, ZonedDateTime}
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
        case (topic, None) => TopicState(topic.id.get, EasinessLevels.EASY, ScoreLevels.EXCELLENT, None, "")
        case (topic, Some(r)) => TopicState(
          topic.id.get,
          r.easiness,
          r.score,
          Some(r.time.toString),
          calcDuration(
            timeInPast = r.time,
            currTime = ZonedDateTime.now()
          )
        )
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

  private val MINUTES_IN_HOUR = 60
  private val HOURS_IN_DAY = 24
  private val SECONDS_IN_MINUTE = 60
  private val SECONDS_IN_HOUR = SECONDS_IN_MINUTE * MINUTES_IN_HOUR
  private val SECONDS_IN_DAY = SECONDS_IN_HOUR * HOURS_IN_DAY
  private val SECONDS_IN_MONTH = SECONDS_IN_DAY * 30
  protected[controllers] def calcDuration(timeInPast: ZonedDateTime, currTime: ZonedDateTime): String = {
    val dur = Duration.between(timeInPast, currTime)
    val days = dur.getSeconds / SECONDS_IN_DAY
    val hours = (dur.getSeconds - days * SECONDS_IN_DAY) / SECONDS_IN_HOUR
    val minutes = (dur.getSeconds - days * SECONDS_IN_DAY - hours * SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
    val seconds = dur.getSeconds % SECONDS_IN_MINUTE

    if (days > 0) {
      s"${days}D ${hours}H"
    } else if (hours > 0) {
      s"${hours}H ${minutes}m"
    } else if (minutes > 0) {
      s"${minutes}m ${seconds}s"
    } else {
      s"${seconds}s"
    }
  }

  protected[controllers] def strToDuration(str: String): Duration = Duration.ofSeconds(
    str.trim.split("""\s+""")
      .map(part => (part.substring(0, part.length - 1), part.charAt(part.length - 1)))
      .groupBy(_._2)
      .mapValues(_.map(_._1.toLong).sum)
      .map {
        case ('s', n) => n
        case ('m', n) => n * SECONDS_IN_MINUTE
        case ('h', n) => n * SECONDS_IN_HOUR
        case ('d', n) => n * SECONDS_IN_DAY
        case ('M', n) => n * SECONDS_IN_MONTH
      }.sum
  )
}
