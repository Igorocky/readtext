package controllers

import java.io.File
import javax.inject._

import db.Tables.{paragraphTable, topicTable}
import db.{Dao, HasIdAndOrder, PrintSchema}
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.{FormKeys, SharedConstants}
import shared.SharedConstants._
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.forms.PostData.{dataResponse, errorResponse, formWithErrors}
import shared.forms.{Forms, PostData}
import shared.pageparams.ListTopicsPageParams
import slick.dbio.DBIOAction
import slick.dbio.Effect.Read
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import upickle.default._
import utils.ServerUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

@Singleton
class TopicController @Inject()(
                                val messagesApi: MessagesApi,
                                protected val dbConfigProvider: DatabaseConfigProvider,
                                val configuration: play.api.Configuration,
                                val dao: Dao
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
            createParagraphUrl = routes.TopicController.createParagraph.url,
            updateParagraphUrl = routes.TopicController.updateParagraph.url,
            deleteParagraphUrl = routes.TopicController.deleteParagraph.url,
            createTopicUrl = routes.TopicController.createTopic.url,
            updateTopicUrl = routes.TopicController.updateTopic.url,
            deleteTopicUrl = routes.TopicController.deleteTopic.url,
            uploadTopicFileUrl = routes.TopicController.uploadTopicImage.url,
            getTopicImgUrl = getTopicImgUrl,
            expandUrl = routes.TopicController.expand.url,
            moveUpParagraphUrl = routes.TopicController.upParagraph.url,
            moveUpTopicUrl = routes.TopicController.upTopic.url,
            moveDownParagraphUrl = routes.TopicController.downParagraph.url,
            moveDownTopicUrl = routes.TopicController.downTopic.url,
            checkParagraphUrl = routes.TopicController.checkParagraph.url,
            checkTopicsUrl = routes.TopicController.checkTopics.url,
            addTagForTopicUrl = routes.TopicController.addTagForTopic.url,
            removeTagFromTopicUrl = routes.TopicController.removeTagFromTopic.url,
            paragraphs = ps
          )),
          pageTitle = "Topics"
        )
      )

    }
  }

  def createParagraph = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations) match {
      case Right(values) =>
        val paragraph = Paragraph(
          name = values(FormKeys.TITLE)
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
          id = values(FormKeys.ID),
          name = values(FormKeys.TITLE)
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
          paragraphId = values(FormKeys.PARAGRAPH_ID),
          title = values(FormKeys.TITLE)
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
          id = values(FormKeys.ID),
          title = values(FormKeys.TITLE),
          images = values(FormKeys.IMAGES)
        )
        db.run(
          for {
            _ <- topicTable.filter(_.id === topic.id).map(t => (t.title, t.images)).update((topic.title, topic.imagesStr))
            top <- topicTable.filter(_.id === topic.id).result.head
          } yield top
        ) map { top =>
          removeUnusedImages(top)
          println(s"action = update topic: $top")
          Ok(dataResponse(write(TopicUpdate(id = top.id.get, title = top.title, images = top.images))))
        }
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  def removeUnusedImages(topic: Topic): Unit = {
    Some(getTopicImagesDir(topic.id.get, imgStorageDir).listFiles()).filterNot(_ eq null).foreach { files =>
      files.filterNot(f => topic.images.exists(_ == f.getName))
        .foreach(_.delete())
    }
  }

  val imgStorageDir = new File(configuration.getString("topicsImgStorageDir").get)
  def uploadTopicImage = Action(parse.multipartFormData) { request =>
    request.body.file(FILE).map { file =>
      val topicId = request.body.dataParts(TOPIC_ID)(0).toLong
      val topicFilesDir = getTopicImagesDir(topicId, imgStorageDir)
      val topicFileName = generateNameForNewFile(topicFilesDir) + getFileExtension(file.filename)
      file.ref.moveTo(new File(topicFilesDir, topicFileName))
      Ok(PostData.dataResponse(topicFileName))
    }.getOrElse {
      Ok(errorResponse("Could not upload file."))
    }
  }

  def getFileExtension(name: String): String = if (name.contains('.')) '.' + name.split('.').last else ""

  def getFileNameWithoutExtension(name: String): String =
    if (name.contains('.')) name.substring(0, name.lastIndexOf('.')) else name

  def getTopicImagesDir(topicId: Long, imgStorageDir: File): File =
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

  val fileNamePattern = """^\d+(\.\w+)?$""".r
  def topicImg(topicId: Long, fileName: String) = Action {
    println(s"fileName = ${fileName}")
    fileName match {
      case fileNamePattern(_) => Ok.sendFile(new File(imgStorageDir + "/" + topicId + "/" + fileName))
      case _ => NotFound(s"File $fileName was not found.")
    }
  }

  def expand = Action.async { request =>
    val ids = read[List[(Long, Boolean)]](request.body.asText.get)
    val trueIds = ids.filter(_._2).map(_._1)
    val falseIds = ids.filter(!_._2).map(_._1)
    db.run(
      DBIO.seq(
        paragraphTable.filter(_.id inSet trueIds).map(_.expanded).update(true),
        paragraphTable.filter(_.id inSet falseIds).map(_.expanded).update(false)
      )
    ) map {_ =>
      Ok(dataResponse(SharedConstants.OK))
    }
  }

  def checkParagraph = Action.async { request =>
    val (id, newVal) = read[(Long, Boolean)](request.body.asText.get)
    db.run(paragraphTable.filter(_.id === id).map(_.checked).update(newVal)) map {_ =>
      Ok(dataResponse(SharedConstants.OK))
    }
  }

  def checkTopics = Action.async { request =>
    val ids = read[List[(Long, Boolean)]](request.body.asText.get)
    val trueIds = ids.filter(_._2).map(_._1)
    val falseIds = ids.filter(!_._2).map(_._1)
    db.run(
      DBIO.seq(
        topicTable.filter(_.id inSet trueIds).map(_.checked).update(true),
        topicTable.filter(_.id inSet falseIds).map(_.checked).update(false)
      )
    ) map {_ =>
      Ok(dataResponse(SharedConstants.OK))
    }
  }

  private def changeOrder[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C], down: Boolean)
                                      = Action.async { request =>
    val id = request.body.asText.get.toLong
    db.run(
      dao.changeOrder(id, table, down).asTry.map {
        case Failure(ex) => errorResponse(ex.getMessage)
        case Success(_) => dataResponse(SharedConstants.OK)
      }
    ) map {resp =>
      Ok(resp)
    }
  }

  def upParagraph = changeOrder(paragraphTable, false)
  def upTopic = changeOrder(topicTable, false)
  def downParagraph = changeOrder(paragraphTable, true)
  def downTopic = changeOrder(topicTable, true)

  def addTagForTopic = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.tagForm.transformations) match {
      case Right(values) =>
        val topId = values(FormKeys.PARENT_ID)
        val tagToAdd = values(FormKeys.TAG)
        db.run(
          for {
            topic <- topicTable.filter(_.id === topId).result.head
            topicWithNewTags = topic.copy(tags = tagToAdd::topic.tags)
            _ <- topicTable.filter(_.id === topId).map(_.tags).update(topicWithNewTags.tagsStr)
          } yield (topicWithNewTags.tags)
        ) map {tagsAfterUpdate =>
          Ok(dataResponse(write(tagsAfterUpdate)))
        }
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  def removeTagFromTopic = Action.async { request =>
    val (topId, tagToRemove) = read[(Long, String)](request.body.asText.get)
    db.run(
      for {
        topic <- topicTable.filter(_.id === topId).result.head
        topicWithNewTags = topic.copy(tags = topic.tags.filterNot(_ == tagToRemove))
        _ <- topicTable.filter(_.id === topId).map(_.tags).update(topicWithNewTags.tagsStr)
      } yield (topicWithNewTags.tags)
    ) map {tagsAfterUpdate =>
      Ok(dataResponse(write(tagsAfterUpdate)))
    }
  }

}
