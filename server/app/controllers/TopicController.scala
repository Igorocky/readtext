package controllers

import java.io.File
import javax.inject._

import db.Tables
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.SharedConstants
import shared.SharedConstants._
import shared.dto.{Paragraph, Topic}
import shared.forms.{Forms, PostData}
import shared.forms.PostData.{dataResponse, formWithErrors}
import shared.pageparams.ListTopicsPageParams
import slick.driver.JdbcProfile
import upickle.default._
import utils.ServerUtils._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TopicController @Inject()(
                                val messagesApi: MessagesApi,
                                protected val dbConfigProvider: DatabaseConfigProvider
                              )(implicit private val environment: Environment,
                                implicit private val ec: ExecutionContext)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with Tables with I18nSupport {

  def topics = Action { implicit request =>
    val pn = 3
    val tn = 6
    val ps = (0 until pn).map{pi => Paragraph(
      id = Some(pi),
      name = "PARAGRAPH-" + pi,
      order = pi,
      topics = (0 until tn).map{ti=>
        Topic(
          id = Some(pi*tn + ti),
          paragraphId = Some(pi),
          title = "topic-" + ti,
          order = ti
        )
      }.toList
    )}.toList
    Ok(
      views.html.univpage(
        pageType = ListTopicsPageParams.getClass.getName,
        customData = write(ListTopicsPageParams(
          headerParams = headerParams(getSession.language),
          doActionUrl = routes.TopicController.doAction.url,
          createParagraphUrl = routes.TopicController.createParagraph.url,
          renameParagraphUrl = routes.TopicController.renameParagraph.url,
          createTopicUrl = routes.TopicController.createTopic.url,
          updateTopicUrl = routes.TopicController.updateTopic.url,
          uploadTopicFileUrl = routes.TopicController.uploadTopicFile.url,
          paragraphs = ps
        ))
      )
    )
  }

  var parId = 5
  var topId = 50
  def doAction = Action{ request =>
//    println("sleep...")
//    Thread.sleep(3000)
    val action = request.body.asText.get
    println(s"action = ${action}")
//    if (Random.nextBoolean()) {
//      Ok(dataResponse(SharedConstants.OK))
//    } else {
//      Ok(errorResponse("Unknown error occurred."))
//    }
    if (action.startsWith("create new paragraph")) {
      parId += 1
      Ok(dataResponse(parId.toString))
    } else {
      Ok(dataResponse(SharedConstants.OK))
    }

  }

  def createParagraph = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations) match {
      case Right(values) =>
        val paragraph = Paragraph(
          name = values(SharedConstants.TITLE).asInstanceOf[String]
        )
        parId += 1
        println(s"action = create paragraph: $paragraph")
        Future.successful(Ok(dataResponse(write(paragraph.copy(id = Some(parId))))))
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  def renameParagraph = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations) match {
      case Right(values) =>
        val paragraph = Paragraph(
          id = values(SharedConstants.ID).asInstanceOf[Option[Long]],
          name = values(SharedConstants.TITLE).asInstanceOf[String]
        )
        println(s"action = rename paragraph: $paragraph")
        Future.successful(Ok(dataResponse(write(paragraph))))
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  def createTopic = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.topicForm.transformations) match {
      case Right(values) =>
        val topic = Topic(
          paragraphId = values(SharedConstants.PARAGRAPH_ID).asInstanceOf[Option[Long]],
          title = values(SharedConstants.TITLE).asInstanceOf[String]
        )
        topId += 1
        println(s"action = create topic: $topic")
        Future.successful(Ok(dataResponse(write(topic.copy(id = Some(topId))))))
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  def updateTopic = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.topicForm.transformations) match {
      case Right(values) =>
        val topic = Topic(
          id = values(SharedConstants.ID).asInstanceOf[Option[Long]],
          paragraphId = values(SharedConstants.PARAGRAPH_ID).asInstanceOf[Option[Long]],
          title = values(SharedConstants.TITLE).asInstanceOf[String]
        )
        println(s"action = update topic: $topic")
        Future.successful(Ok(dataResponse(write(topic))))
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  val imgStorageDir = new File("D:/temp/uploaded")
  def uploadTopicFile = Action(parse.multipartFormData) { request =>
    println(">in upload")
    println(s"request.body.dataParts = ${request.body.dataParts}")
    request.body.file(FILE).map { file =>
      val paragraphId = request.body.dataParts(PARAGRAPH_ID)(0).toLong
      val topicId = request.body.dataParts(TOPIC_ID)(0).toLong
      val topicFilesDir = getTopicFilesDir(paragraphId, topicId, imgStorageDir)
      val topicFileName = generateNameForNewFile(topicFilesDir)
      file.ref.moveTo(new File(topicFilesDir, topicFileName))
      Ok(PostData.dataResponse(topicFileName))
    }.getOrElse {
      Ok(PostData.errorResponse("Could not upload file."))
    }
  }

  def getTopicFilesDir(paragraphId: Long, topicId: Long, imgStorageDir: File): File =
    new File(imgStorageDir, s"$paragraphId/$topicId")

  def generateNameForNewFile(topicFilesDir: File): String = {
    topicFilesDir.mkdirs()
    val existingFileNames = topicFilesDir.listFiles().view.map(_.getName).filter(_.forall(_.isDigit)).map(_.toLong)
    val res = if (existingFileNames.isEmpty) 0 else existingFileNames.max + 1
    res.toString
  }
}
