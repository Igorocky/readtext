package controllers

import java.io.File
import javax.inject._

import db.Tables.{paragraphTable, topicTable}
import db.{PrintSchema, Tables, TopicTable}
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.SharedConstants
import shared.SharedConstants._
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.forms.{Forms, PostData}
import shared.forms.PostData.{dataResponse, formWithErrors}
import shared.pageparams.ListTopicsPageParams
import slick.dbio.DBIOAction
import slick.dbio.Effect.Read
import slick.driver.JdbcProfile
import upickle.default._
import utils.ServerUtils._

import scala.concurrent.{ExecutionContext, Future}
import slick.driver.H2Driver.api._
import slick.lifted.QueryBase
import slick.profile.FixedSqlStreamingAction

import scalaz.Maybe

@Singleton
class TopicController @Inject()(
                                val messagesApi: MessagesApi,
                                protected val dbConfigProvider: DatabaseConfigProvider,
                                val configuration: play.api.Configuration
                              )(implicit private val environment: Environment,
                                implicit private val ec: ExecutionContext)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  new PrintSchema

  val getTopicImgUrl = """/\w+""".r.findFirstIn(routes.TopicController.topicImg(1,"").url).get
  def topics = Action.async { implicit request =>

    val ps: DBIOAction[List[Paragraph], NoStream, Read with Read] = for {
      ts <- topicTable.sortBy(_.order).result.map(_.groupBy(_.paragraphId) mapValues (_.toList) withDefaultValue(Nil))
      ps <- paragraphTable.sortBy(_.order).result
    } yield ps.map(p => p.copy(topics = ts(p.id))).toList

    db.run(ps).map{ps=>
      Ok(
        views.html.univpage(
          pageType = ListTopicsPageParams.getClass.getName,
          customData = write(ListTopicsPageParams(
            headerParams = headerParams(getSession.language),
            doActionUrl = routes.TopicController.doAction.url,
            createParagraphUrl = routes.TopicController.createParagraph.url,
            updateParagraphUrl = routes.TopicController.updateParagraph.url,
            deleteParagraphUrl = routes.TopicController.deleteParagraph.url,
            createTopicUrl = routes.TopicController.createTopic.url,
            updateTopicUrl = routes.TopicController.updateTopic.url,
            deleteTopicUrl = routes.TopicController.deleteTopic.url,
            uploadTopicFileUrl = routes.TopicController.uploadTopicImage.url,
            getTopicImgUrl = getTopicImgUrl,
            paragraphs = ps
          ))
        )
      )

    }
  }

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
      Ok(dataResponse(SharedConstants.OK))
  }

  def createParagraph = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations) match {
      case Right(values) =>
        val paragraph = Paragraph(
          name = values(SharedConstants.TITLE).asInstanceOf[String]
        )
        db.run(for {
          maxOrder <- paragraphTable.map(_.order).max.result.map(_.getOrElse(0))
          par = paragraph.copy(order = maxOrder + 1)
          newId <- paragraphTable returning paragraphTable.map(_.id) += par
        } yield par.copy(id = Some(newId))) map {par=>
          println(s"action = create paragraph: $par")
          Ok(dataResponse(write(par)))
        }
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

//  def deleteParagraph = Action.async { request =>
//    val parId = request.body.asText.get.toLong
//    db.run(for {
//      deletedPar <- paragraphTable.filter(_.id === parId).result.head
//      _ <- paragraphTable.filter(_.id === parId).delete
//      seq <- paragraphTable.filter(_.order > deletedPar.order).map(p => (p.id,p.order)).result
//      _ <- DBIO.sequence(seq.map{
//        case (movingParId, movingParOrder) =>
//          paragraphTable.filter(_.id === movingParId).map(_.order).update(movingParOrder - 1)
//      })
//    } yield ()) map {_ =>
//      Ok(dataResponse(SharedConstants.OK))
//    }
//  }

  def deleteParagraph = Action.async { request =>
    val parId = request.body.asText.get.toLong
    db.run(for {
      deletedParOrder <- paragraphTable.filter(_.id === parId).map(_.order).result.head
      _ <- paragraphTable.filter(_.id === parId).delete
      seq <- paragraphTable.filter(_.order > deletedParOrder).map(p => (p.id,p.order)).result
      _ <- DBIO.sequence(for {
        (lowerParId, lowerParOrder) <- seq
      } yield paragraphTable.filter(_.id === lowerParId).map(_.order).update(lowerParOrder - 1))
    } yield ()) map {_ =>
      Ok(dataResponse(SharedConstants.OK))
    }
  }

  def updateParagraph = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations) match {
      case Right(values) =>
        val paragraph = Paragraph(
          id = values(SharedConstants.ID).asInstanceOf[Option[Long]],
          name = values(SharedConstants.TITLE).asInstanceOf[String]
        )
        db.run(
          for {
            _ <- paragraphTable.filter(_.id === paragraph.id).map(_.name).update(paragraph.name)
          } yield ()
        ) map {_=>
          println(s"action = rename paragraph: $paragraph")
          Ok(dataResponse(write(ParagraphUpdate(id = paragraph.id.get, name = paragraph.name))))
        }
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
        db.run(for {
          maxOrder <- topicTable.filter(_.paragraphId === topic.paragraphId.get).map(_.order).max.result.map(_.getOrElse(0))
          top = topic.copy(order = maxOrder + 1)
          newId <- topicTable returning topicTable.map(_.id) += top
        } yield top.copy(id = Some(newId))) map {top=>
          println(s"topic created - $top")
          Ok(dataResponse(write(top)))
        }
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

//  def deleteTopic = Action.async { request =>
//    val topId = request.body.asText.get.toLong
//    db.run(for {
//      deletedOrder <- topicTable.filter(_.id === topId).map(_.order).result.head
//      _ <- topicTable.filter(_.id === topId).delete
//      seq <- topicTable.filter(_.order > deletedOrder).map(t => (t.id,t.order)).result
//      _ <- DBIO.sequence(seq.map{
//        case (movingTopId, movingTopOrder) =>
//          topicTable.filter(_.id === movingTopId).map(_.order).update(movingTopOrder - 1)
//      })
//    } yield ()) map {_ =>
//      Ok(dataResponse(SharedConstants.OK))
//    }
//  }

  def deleteTopic = Action.async { request =>
    val topId = request.body.asText.get.toLong
    db.run(for {
      (deletedOrder, parId) <- topicTable.filter(_.id === topId).map(t => (t.order, t.paragraphId)).result.head
      _ <- topicTable.filter(_.id === topId).delete
      seq <- topicTable.filter(t => t.paragraphId === parId && t.order > deletedOrder).map(t => (t.id,t.order)).result
      _ <- DBIO.sequence(for {
        (lowerTopId, lowerTopOrder) <- seq
      } yield topicTable.filter(_.id === lowerTopId).map(_.order).update(lowerTopOrder - 1))
    } yield ()) map {_ =>
      Ok(dataResponse(SharedConstants.OK))
    }
  }


  def updateTopic = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.topicForm.transformations) match {
      case Right(values) =>
        val topic = Topic(
          id = values(SharedConstants.ID).asInstanceOf[Option[Long]],
          title = values(SharedConstants.TITLE).asInstanceOf[String],
          images = values(SharedConstants.IMAGES).asInstanceOf[List[String]]
        )
        db.run(
          for {
            _ <- topicTable.filter(_.id === topic.id).map(t => (t.title, t.images)).update((topic.title, topic.imagesStr))
            top <- topicTable.filter(_.id === topic.id).result.head
          } yield top
        ) map { top =>
          println(s"action = update topic: $top")
          Ok(dataResponse(write(TopicUpdate(id = top.id.get, title = top.title, images = top.images))))
        }
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  val imgStorageDir = new File(configuration.getString("topicsImgStorageDir").get)
  def uploadTopicImage = Action(parse.multipartFormData) { request =>
    request.body.file(FILE).map { file =>
      val topicId = request.body.dataParts(TOPIC_ID)(0).toLong
      val topicFilesDir = getTopicFilesDir(topicId, imgStorageDir)
      val topicFileName = generateNameForNewFile(topicFilesDir) + getFileExtension(file.filename)
      file.ref.moveTo(new File(topicFilesDir, topicFileName))
      Ok(PostData.dataResponse(topicFileName))
    }.getOrElse {
      Ok(PostData.errorResponse("Could not upload file."))
    }
  }

  def getFileExtension(name: String): String = if (name.contains('.')) '.' + name.split('.').last else ""

  def getFileNameWithoutExtension(name: String): String =
    if (name.contains('.')) name.substring(0, name.lastIndexOf('.')) else name

  def getTopicFilesDir(topicId: Long, imgStorageDir: File): File =
    new File(imgStorageDir, s"/$topicId")

  def generateNameForNewFile(topicFilesDir: File): String = {
    topicFilesDir.mkdirs()
    val existingFileNames = topicFilesDir.listFiles().view
      .map(_.getName)
      .map(getFileNameWithoutExtension)
      .filter(_.forall(_.isDigit))
      .map(_.toLong)
    val res = if (existingFileNames.isEmpty) 0 else existingFileNames.max + 1
    res.toString
  }

  val fileNamePattern = """^\d+\.\w+$""".r
  def topicImg(topicId: Long, fileName: String) = Action {
    println(s"fileName = ${fileName}")
    fileName match {
      case fileNamePattern() => Ok.sendFile(new File(imgStorageDir + "/" + topicId + "/" + fileName))
      case _ => NotFound(s"File $fileName was not found.")
    }
  }
}
