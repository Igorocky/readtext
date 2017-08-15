package controllers

import java.time.{Clock, Duration, ZonedDateTime}
import javax.inject._

import app.RouterBuilderUtils
import db.{DaoCommon, TypeConversions}
import TypeConversions._
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
    .addHandler(forMethod(_.loadCardState))(loadTopicState)
    .addHandler(forMethod(_.loadCardHistory))(loadCardHistory)
    .addHandler(forMethod(_.loadNewTopics))(loadNewTopics)

    .addHandler(forMethod2(_.loadActiveTopics)){
      case (paragraphId, activationTimeReduction) => loadActiveTopics(paragraphId, activationTimeReduction)
    }

    .addHandler(forMethod2(_.updateCardState)) {
      case (cardId, commentAndScore) => updateTopicState(cardId, commentAndScore).map(_ => Right(Unit))
    }

    .addHandler(forMethod(_.loadTopic)) {
      case topicId => db.run(
        topicTable.filter(_.id === topicId).result.head
      )
    }

  protected[controllers] def loadCardHistory(topicId: Long): Future[List[List[String]]] = {
    val header = List("Time", "abs. duration", "estimated", "overtime", "comment")
    db.run(
      topicHistoryTable.filter(_.topicId === topicId).sortBy(_.time).result
    )
      .map(_.map(Some(_)))
      .map(Seq(None) ++ _)
      .map(seq => seq zip seq.tail)
      .map(_.map {
        case (None, Some(cur)) => List(cur.time.toString, "", "", "", cur.comment)
        case (Some(prev), Some(cur)) =>
          val actualDuration = Duration.between(prev.time, cur.time).getSeconds
          List(
            cur.time.toString,
            secondsDurationToStr(actualDuration),
            secondsDurationToStr(prev.score),
            calcOvertime(prev.score, actualDuration),
            cur.comment
          )
      })
      .map(_.toList.reverse)
      .map(header::_)
  }

  private def calcOvertime(estimated: Long, actual: Long): String = {
    val diffAbs = secondsDurationToStr((estimated - actual).abs)
    (if (actual < estimated) '-' else '+') + diffAbs
  }

  protected[controllers] def loadTopicState(topicId: Long): Future[Option[TopicState]] = {
    val currTime = ZonedDateTime.now(clock)
    for {
      topics <- loadTopicCurrHistRecs(List(topicId))
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
          comment = r.comment,
          activationTime = r.activationTime.toString,
          isActive = isActive,
          timeLeftUntilActivation = if (!isActive) Some(secondsDurationToStr(secondsUntilActivation)) else None,
          timePassedAfterActivation = if (isActive) Some(secondsDurationToStr(-secondsUntilActivation)) else None
        )))
      }
    }
  }.map(_.head._2)

  protected[controllers] def updateTopicState(topicId: Long, commentAndScore: String): Future[Unit] = {
    val Array(comment, scoreStr) = commentAndScore.split(";")
    updateTopicState(topicId, comment, strToDuration(scoreStr).getSeconds)
  }


  protected[controllers] def updateTopicState(topicId: Long, comment: String, score: Long): Future[Unit] = {
    val currTime = ZonedDateTime.now(clock)
    val historyRecord = TopicHistoryRecord(
      topicId = topicId,
      score = score,
      comment = comment,
      time = currTime,
      activationTime = currTime.plusSeconds(score)
    )
    db.run(DBIO.seq(
      topicHistoryTable += historyRecord,
      topicStateTable.insertOrUpdate(historyRecord)
    ).transactionally)
  }

  protected[controllers] def loadParagraphsRecursively(parentParagraphIds: Seq[Long]): Future[Seq[Long]] = for {
      childParagraphs <- db.run(paragraphTable.filter(_.parentId inSet(parentParagraphIds)).map(_.id).result)
      rest <- if (childParagraphs.isEmpty) Future.successful(Seq()) else loadParagraphsRecursively(childParagraphs)
    } yield parentParagraphIds++rest

  protected[controllers] def loadTopicIdsRecursively(paragraphId: Long): Future[Seq[Long]] = {
    for {
      paragraphIds <- loadParagraphsRecursively(List(paragraphId))
      res <- db.run(
        topicTable.filter(_.paragraphId inSet(paragraphIds)).map(_.id).result
      )
    } yield res
  }

  protected[controllers] def loadTopicCurrHistRecs(topicIds: Seq[Long]): Future[Seq[(Topic, Option[TopicHistoryRecord])]] = {
    for {
      res <- db.run(
        topicTable.filter(_.id inSet(topicIds))
          .joinLeft(topicStateTable).on(_.id === _.topicId)
          .result
      )
    } yield res
  }

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

  protected[controllers] def loadActiveTopics(paragraphId: Long,
                                              activationTimeReduction: Option[String]): Future[List[Topic]] = {
    val currTime = ZonedDateTime.now(clock)
    val reductionSeconds = strToDuration(activationTimeReduction.getOrElse("0s")).getSeconds
    (for {
      topicIds <- loadTopicIdsRecursively(paragraphId)
      topics <- loadTopicCurrHistRecs(topicIds)
    } yield topics).map{
      _.filter(_._2.isDefined)
        .map{case (t, Some(h)) => (t, Duration.between(h.activationTime, currTime).getSeconds + reductionSeconds)}
        .filter{case (t, overtime) => overtime >= 0}
        .sortBy(t => -t._2)
        .map(_._1)
        .toList
    }
  }

  protected[controllers] def loadNewTopics(paragraphId: Long): Future[List[Topic]] = for {
    tree <- loadNewTopics(TopicTreeServ(Some(Paragraph(id = Some(paragraphId), name = "rootPar"))))
  } yield tree.findNodes {
    case TopicTreeServ(Some(_:Topic), _) => true
    case _ => false
  }.map(_.value.get.asInstanceOf[Topic])


  private def loadNewTopics(tree: TopicTreeServ): Future[TopicTreeServ] = tree.findNodes {
    case TopicTreeServ(Some(par: Paragraph), None) => true
    case _ => false
  } match {
    case Nil => Future.successful(tree)
    case TopicTreeServ(Some(par: Paragraph), _) :: _ => loadNewTopics(tree, par).flatMap{newTree=>
      if (newTree eq tree) Future.successful(tree)
      else loadNewTopics(newTree)
    }
  }

  private def loadNewTopics(tree: TopicTreeServ, parent: Paragraph): Future[TopicTreeServ] = {
    for {
      paragraphs <- db.run(dao.loadOrderedChildren(paragraphTable, parent.id))
      topics <- db.run(
        topicTable.filter(_.paragraphId === parent.id)
          .joinLeft(topicStateTable).on(_.id === _.topicId)
          .filter(_._2.isEmpty)
          .map(_._1)
          .sortBy(_.order)
          .result
      )
    } yield paragraphs ++ topics
  }.map{children=>
    tree.setChildren(parent.id, children.map(ch => TopicTreeServ(value = Some(ch))).toList)
  }
}
