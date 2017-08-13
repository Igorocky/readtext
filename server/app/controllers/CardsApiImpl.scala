package controllers

import java.time.{Clock, Duration, ZonedDateTime}
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
                             val dao: DaoCommon,
                             val clock: Clock
                            )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with RouterBuilderUtils[CardsApi] {

  var questionCnt = 0

  val router: Router = RouterBuilder()
    // TODO: use timezone of the user
//    .addHandler(forMethod(_.loadCardStates))(loadTopicStates)
//    .addHandler(("shared.api.CardsApi.loadCardStates", upickle.default.read[Long](_), (a: List[TopicStatePair]) => upickle.default.write(a)(upickle.default.Writer[List[TopicStatePair]])))(loadTopicStates)

    .addHandler(forMethod2(_.updateCardState)) {
      case (cardId, score) => updateTopicState(cardId, score)
    }

    .addHandler(forMethod(_.loadTopic)) {
      case topicId => db.run(
        topicTable.filter(_.id === topicId).result.head
      )
    }

  protected[controllers] def loadTopicStates(paragraphId: Long): Future[List[(Long, Option[TopicState])]] = {
    val currTime = ZonedDateTime.now(clock)
    for {
      topics <- loadTopicsRecursively(paragraphId)
    } yield topics.map{
      case (topic, None) => (topic.id.get, None)
      case (topic, Some(r)) => {
        val secondsUntilActivation = Duration.between(currTime, r.activationTime).getSeconds
        val isActive = secondsUntilActivation <= 0
        (topic.id.get, Some(TopicState(
          timeOfChange = r.time.toString,
          lastChangedDuration = calcDuration(
            timeInPast = r.time,
            currTime = currTime
          ),
          score = secondsDurationToStr(r.score),
          activationTime = r.activationTime.toString,
          isActive = isActive,
          timeLeftUntilActivation = if (!isActive) Some(secondsDurationToStr(secondsUntilActivation)) else None,
          timePassedAfterActivation = if (isActive) Some(secondsDurationToStr(-secondsUntilActivation)) else None
        )))
      }
    }
  }

  protected[controllers] def updateTopicState(topicId: Long, score: String): Future[Unit] =
    updateTopicState(topicId, strToDuration(score).getSeconds)

  protected[controllers] def updateTopicState(topicId: Long, score: Long): Future[Unit] = {
    val currTime = ZonedDateTime.now(clock)
    val historyRecord = TopicHistoryRecord(
      topicId = topicId,
      score = score,
      time = currTime,
      activationTime = currTime.plusSeconds(score)
    )
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
    secondsDurationToStr(Duration.between(timeInPast, currTime).getSeconds)
  }

  protected[controllers] def secondsDurationToStr(durationSeconds: Long): String = {
    val months = durationSeconds / SECONDS_IN_MONTH
    val days = (durationSeconds - months * SECONDS_IN_MONTH) / SECONDS_IN_DAY
    val hours = (durationSeconds - months * SECONDS_IN_MONTH - days * SECONDS_IN_DAY) / SECONDS_IN_HOUR
    val minutes = (durationSeconds - months * SECONDS_IN_MONTH - days * SECONDS_IN_DAY - hours * SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
    val seconds = durationSeconds % SECONDS_IN_MINUTE

    if (months > 0) {
      s"${months}M ${days}d"
    } else if (days > 0) {
      s"${days}d ${hours}h"
    } else if (hours > 0) {
      s"${hours}h ${minutes}m"
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
