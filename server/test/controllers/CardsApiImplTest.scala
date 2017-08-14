package controllers

import java.time._

import db.Tables._
import db.{DaoCommon, DbTestHelperWithTables, TypeConversions}
import TypeConversions._
import shared.dto._
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
class CardsApiImplTest extends DbTestHelperWithTables {

  override protected val tables = List(paragraphTable, topicTable, topicHistoryTable, topicStateTable)

  def createParagraph(parentId: Option[Long], name: String): Option[Long] = Some(db.run(
    paragraphTable returning paragraphTable.map(_.id) += Paragraph(name = name, paragraphId = parentId)
  ).futureValue)

  def loadParagraphs(ids: List[Long]): Seq[Paragraph] = db.run(
    paragraphTable.filter(_.id inSet(ids)).result
  ).futureValue

  def loadParagraphs(names: String*): Seq[Paragraph] = db.run(
    paragraphTable.filter(_.name inSet(names)).result
  ).futureValue

  def createTopic(parentId: Option[Long], name: String): Long = db.run(
    topicTable returning topicTable.map(_.id) += Topic(paragraphId = parentId.get, title = name)
  ).futureValue

  def loadTopics(names: String*): Seq[Topic] = db.run(
    topicTable.filter(_.title inSet(names)).result
  ).futureValue

  def loadTopicHistory(topicId: Long): Seq[TopicHistoryRecord] = db.run(
    topicHistoryTable.filter(_.topicId === topicId).result
  ).futureValue

  def loadTopicState(topicId: Long): Seq[TopicHistoryRecord] = db.run(
    topicStateTable.filter(_.topicId === topicId).result
  ).futureValue

  def setTopicState(topicId: Long, score: Long, activationTime: ZonedDateTime, time: ZonedDateTime) = db.run(
    topicStateTable += TopicHistoryRecord(
      topicId = topicId,
      score = score,
      comment = "",
      activationTime = activationTime,
      time = time
    )
  ).futureValue

  val timeZone = ZoneId.of("Europe/Paris")
  val currTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, timeZone)

  val clock = new Clock {
    private var cTime = currTime
    override def withZone(zone: ZoneId): Clock = {
      cTime.withZoneSameInstant(zone)
      this
    }
    override def getZone: ZoneId = cTime.getZone
    override def instant(): Instant = cTime.toInstant
    def setTime(newTime: ZonedDateTime) = cTime = newTime
  }

  val cardsApiImpl: CardsApiImpl = new CardsApiImpl(
    dbConfigProvider,
    new DaoCommon,
    clock
  )

  def buildTopicTree = {
    /*
     p1
     +--p2
     |  +--p5
     |  |  +--p11
     |  |  |  +--t1
     |  |  |  +--t2
     |  |  +--p12
     |  |  |  +--t3
     |  |  |  +--t4
     |  +--p6
     |  |  +--p13
     |  |     +--t5
     |  +--p7
     |     +--t6
     +--p3
     |  +--p8
     |     +--t7
     +--p4
        +--p9
        |  +--t8
        +--p10
        |  +--t9
        +--t10

    */
    val p1 = createParagraph(None, "p1")
    val p2 = createParagraph(p1, "p2")
    val p3 = createParagraph(p1, "p3")
    val p4 = createParagraph(p1, "p4")
    val p5 = createParagraph(p2, "p5")
    val p6 = createParagraph(p2, "p6")
    val p7 = createParagraph(p2, "p7")
    val p8 = createParagraph(p3, "p8")
    val p9 = createParagraph(p4, "p9")
    val p10 = createParagraph(p4, "p10")
    val p11 = createParagraph(p5, "p11")
    val p12 = createParagraph(p5, "p12")
    val p13 = createParagraph(p6, "p13")

    val t1 = createTopic(p11, "t1")
    val t2 = createTopic(p11, "t2")
    val t3 = createTopic(p12, "t3")
    val t4 = createTopic(p12, "t4")
    val t5 = createTopic(p13, "t5")
    val t6 = createTopic(p7, "t6")
    val t7 = createTopic(p8, "t7")
    val t8 = createTopic(p9, "t8")
    val t9 = createTopic(p10, "t9")
    val t10 = createTopic(p4, "t10")
  }

  override def localBeforeEach: Unit = {
    clock.setTime(currTime)
  }

  "loadParagraphsRecursively should return all paragraphs" in {
    //given
    buildTopicTree
    val p2 = loadParagraphs("p2").head

    //when
    val ids = cardsApiImpl.loadParagraphsRecursively(List(p2.id.get)).futureValue

    //then
    val paragraphs = loadParagraphs(ids)
    paragraphs.size should be(7)
    paragraphs.map(_.name).toSet should be(Set("p2", "p5", "p6", "p7", "p11", "p12", "p13"))
  }

  "loadTopicsRecursively should return correct topic state" in {
    //given
    buildTopicTree
    val t8 = loadTopics("t8").head
    val timeZone = ZoneId.of("America/Anchorage")
    val time = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, timeZone)
    setTopicState(t8.id.get, 100, time.plusSeconds(100), time)

    val p9 = loadParagraphs("p9").head

    //when
    val (_, Some(histRec)) = cardsApiImpl.loadTopicsRecursively(p9.id.get).futureValue.head

    //then
    histRec.copy(
      time = histRec.time.withZoneSameInstant(timeZone),
      activationTime = histRec.activationTime.withZoneSameInstant(timeZone)
    ) should be(TopicHistoryRecord(
      topicId = t8.id.get,
      score = 100,
      comment = "",
      activationTime = time.plusSeconds(100),
      time = time
    ))
  }
  "loadTopicsRecursively should return None as history record for a topic without history" in {
    //given
    buildTopicTree
    val p4 = loadParagraphs("p4").head

    //when
    val topics = cardsApiImpl.loadTopicsRecursively(p4.id.get).futureValue

    //then
    topics.find(_._1.title == "t8").get._2 should be(None)
  }
  "loadTopicsRecursively should return all topics recursively" in {
    //given
    buildTopicTree
    val p4 = loadParagraphs("p4").head

    //when
    val names = cardsApiImpl.loadTopicsRecursively(p4.id.get).futureValue.map(_._1.title)

    //then
    names.size should be(3)
    names.toSet should be(Set("t10","t8","t9"))
  }
  "loadTopicsRecursively should return all topics sorted by time in asc order" in {
    //given
    buildTopicTree
    val t8 = loadTopics("t8").head
    val t9 = loadTopics("t9").head
    val t10 = loadTopics("t10").head
    val timeZone = ZoneOffset.UTC
    val time = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, timeZone)
    setTopicState(t8.id.get, 100, time.plusSeconds(100), time)
    setTopicState(t9.id.get, 100, time.plusSeconds(100), time.minusMinutes(5))
    setTopicState(t10.id.get, 100, time.plusSeconds(100), time.plusMinutes(3))

    val p4 = loadParagraphs("p4").head

    //when
    val topics = cardsApiImpl.loadTopicsRecursively(p4.id.get).futureValue.map(_._1.title)

    //then
    topics should be(List("t9","t8","t10"))
  }

  "updateTopicState should append records to history table" in {
    //given
    buildTopicTree
    val t8 = loadTopics("t8").head

    //then
    loadTopicHistory(t8.id.get).isEmpty should be(true)

    //when
    cardsApiImpl.updateTopicState(t8.id.get, "", 100).futureValue
    clock.setTime(currTime.plusSeconds(2))

    //then
    val hist1 = loadTopicHistory(t8.id.get)
    hist1.size should be(1)
    hist1.exists(r => r.score == 100) should be(true)

    //when
    cardsApiImpl.updateTopicState(t8.id.get, "", 200).futureValue

    //then
    val hist2 = loadTopicHistory(t8.id.get)
    hist2.size should be(2)
    hist2.exists(r => r.score == 100) should be(true)
    hist2.exists(r => r.score == 200) should be(true)
  }
  "updateTopicState should update record in topic state table" in {
    //given
    buildTopicTree
    val t8 = loadTopics("t8").head

    //then
    loadTopicState(t8.id.get).isEmpty should be(true)

    //when
    cardsApiImpl.updateTopicState(t8.id.get, "", 100).futureValue
    clock.setTime(currTime.plusSeconds(2))

    //then
    val state1 = loadTopicState(t8.id.get)
    state1.size should be(1)
    state1.exists(r => r.score == 100) should be(true)

    //when
    cardsApiImpl.updateTopicState(t8.id.get, "", 200).futureValue

    //then
    val state2 = loadTopicState(t8.id.get)
    state2.size should be(1)
    state2.exists(r => r.score == 200) should be(true)
  }
  "updateTopicState should calculate activationTime" in {
    //given
    buildTopicTree
    val t8 = loadTopics("t8").head
    val timeZone = ZoneOffset.UTC
    val time = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, timeZone)

    //when
    cardsApiImpl.updateTopicState(t8.id.get, "", 100).futureValue

    //then
    val Vector(hist) = loadTopicHistory(t8.id.get)
    Duration.between(hist.time, hist.activationTime).getSeconds should be(100)
    val Vector(state) = loadTopicState(t8.id.get)
    Duration.between(state.time, state.activationTime).getSeconds should be(100)
  }

  "calcDuration should produce correct output" in {
    var timeInPast = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, ZoneOffset.UTC)
    var currenTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("0s")

    timeInPast = ZonedDateTime.of(2017, 8, 6, 21, 49, 4, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 5, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("1s")

    timeInPast = ZonedDateTime.of(2017, 8, 6, 21, 48, 5, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 5, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("1m 0s")

    timeInPast = ZonedDateTime.of(2017, 8, 6, 21, 11, 23, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 5,  0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("37m 42s")

    timeInPast = ZonedDateTime.of(2017, 8, 6, 9, 48, 6, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 5, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("12h 0m")

    timeInPast = ZonedDateTime.of(2017, 8, 6, 9, 48, 5, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 49, 5, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("12h 1m")

    timeInPast = ZonedDateTime.of(2017, 8, 2, 9, 48, 6, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 48, 5, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("4d 11h")

    timeInPast = ZonedDateTime.of(2017, 8, 2, 9, 48, 6, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 48, 6, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("4d 12h")

    timeInPast = ZonedDateTime.of(2017, 6, 2, 9, 48, 6, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 21, 48, 6, 0, ZoneOffset.UTC)
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("2M 5d")

    timeInPast = ZonedDateTime.of(2017, 8, 6, 9, 48, 6, 0, ZoneOffset.UTC)
    currenTime = ZonedDateTime.of(2017, 8, 6, 12, 50, 6, 0, ZoneId.of("Europe/Paris"))
    cardsApiImpl.calcDuration(timeInPast, currenTime) should be("1h 2m")
  }

  "strToDuration should produce correct output" in {
    cardsApiImpl.strToDuration("5s").getSeconds should be(5)
    cardsApiImpl.strToDuration("6m").getSeconds should be(6*60)
    cardsApiImpl.strToDuration("2h").getSeconds should be(2*60*60)
    cardsApiImpl.strToDuration("3d").getSeconds should be(3*24*60*60)
    cardsApiImpl.strToDuration("5M").getSeconds should be(5*30*24*60*60)

    cardsApiImpl.strToDuration(" 2M\t18d 11h    37m 8s\r\n").getSeconds should be(
      2*30*24*60*60 + 18*24*60*60 + 11*60*60 + 37*60 + 8
    )
  }

  "loadTopicStates should return None for a topic without history" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get

    //when
    val List((_, state)) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state should be(None)
  }
  "loadTopicStates should return correct timeOfChange for a topic with history" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.timeOfChange should be("2017-08-06T19:49:04Z")
  }
  "loadTopicStates should return correct lastChangedDuration for a topic with history" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("7d 3h").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.lastChangedDuration should be("7d 3h")
  }
  "loadTopicStates should return correct score for a topic with history" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.score should be("7d 0h")
  }
  "loadTopicStates should return correct activationTime for a topic with history" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.activationTime should be("2017-08-13T19:49:04Z")
  }
  "loadTopicStates should return isActive==false for an inactive topic" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("6d").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.isActive should be(false)
  }
  "loadTopicStates should return isActive==true for an active topic" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("7d 1s").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.isActive should be(true)
  }
  "loadTopicStates should return correct timeLeftUntilActivation for an inactive topic" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("5d 3h").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.timeLeftUntilActivation should be(Some("1d 21h"))
  }
  "loadTopicStates should return timeLeftUntilActivation==None for an active topic" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("10d 3h").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.timeLeftUntilActivation should be(None)
  }
  "loadTopicStates should return timePassedAfterActivation==None for an inactive topic" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("3d 14h").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.timePassedAfterActivation should be(None)
  }
  "loadTopicStates should return correct timePassedAfterActivation for an active topic" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, ";7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("10d 7h").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.timePassedAfterActivation should be(Some("3d 7h"))
  }
  "loadTopicStates should return correct comment" in {
    //given
    buildTopicTree
    val p9 = loadParagraphs("p9").head.id.get
    val t8 = loadTopics("t8").head.id.get
    cardsApiImpl.updateTopicState(t8, "cmm mt 12344 56 7sdf g dsfg;7d")
    clock.setTime(currTime.plusSeconds(cardsApiImpl.strToDuration("10d 7h").getSeconds))

    //when
    val List((_, Some(state))) = cardsApiImpl.loadTopicStates(p9).futureValue

    //then
    state.comment should be("cmm mt 12344 56 7sdf g dsfg")
  }
}
