package controllers

import java.io.File
import javax.inject._

import db.Tables.{paragraphTable, topicTable}
import db.{DaoCommon, HasIdAndOrder, PrintSchema}
import db.TypeConversions._
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.SharedConstants._
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.forms.PostData.{dataResponse, errorResponse, formWithErrors}
import shared.forms._
import shared.pageparams.ListTopicsPageParams
import shared.SharedConstants
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
                                val dao: DaoCommon
                              )(implicit private val environment: Environment,
                                implicit private val ec: ExecutionContext)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  new PrintSchema

  val getTopicImgUrl = """/\w+""".r.findFirstIn(routes.TopicController.topicImg(1,"").url).get
  def topics = Action { implicit request =>
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
          addTagForTopicUrl = routes.TopicController.addTagForTopic.url,
          removeTagFromTopicUrl = routes.TopicController.removeTagFromTopic.url,
          loadParagraphsByParentIdUrl = routes.TopicController.loadParagraphsByParentId.url,
          loadTopicsByParentIdUrl = routes.TopicController.loadTopicsByParentId.url
        )),
        pageTitle = "Topics"
      )
    )
  }

  def createParagraph = formPostRequest(
    formParts = Forms.paragraphForm,
    onValidForm = values => {
      val paragraph = Paragraph(name = values(FormKeys.TITLE))
      runDbActionAndCreateHttpResponse(
        dao.insertOrdered(paragraphTable)(
          parentId = 0,
          updateOrder = ord => paragraph.copy(order = ord),
          updateId = (par, newId) => par.copy(id = Some(newId))
        ).map(write[Paragraph](_))
      )
    }
  )

  def deleteParagraph = idPostRequest(
    id => runDbActionAndCreateHttpResponse(
      dao.deleteOrdered(paragraphTable, id).map(_ => SharedConstants.OK)
    )
  )

  def updateParagraph = formPostRequest(
    formParts = Forms.paragraphForm,
    onValidForm = values => {
      val parId = values(FormKeys.ID).get
      runDbActionAndCreateHttpResponse(
        dao.updateField(paragraphTable)(parId, _.name)(_ => values(FormKeys.TITLE))
          .map(newName => write(ParagraphUpdate(id = parId, name = newName)))
      )
    }
  )

  def createTopic = formPostRequest(
    formParts = Forms.topicForm,
    onValidForm = values => {
      val topic = Topic(paragraphId = values(FormKeys.PARAGRAPH_ID), title = values(FormKeys.TITLE))
      runDbActionAndCreateHttpResponse(
        dao.insertOrdered(topicTable)(
          parentId = topic.paragraphId.get,
          updateOrder = ord => topic.copy(order = ord),
          updateId = (top, newId) => top.copy(id = Some(newId))
        ).map(write[Topic](_))
      )
    }
  )

  def deleteTopic = idPostRequest(
    id => runDbActionAndCreateHttpResponse(
      dao.deleteOrdered(topicTable, id).map(_ => SharedConstants.OK)
    )
  )

  def updateTopic = formPostRequest(
    formParts = Forms.topicForm,
    onValidForm = values => {
      val topic = Topic(id = values(FormKeys.ID), title = values(FormKeys.TITLE), images = values(FormKeys.IMAGES))
      runDbActionAndCreateHttpResponse(
        topicTable.filter(_.id === topic.id).map(t => (t.title, t.images)).update((topic.title, topic.images))
          .map{_ =>
            removeUnusedImages(topic.id.get, topic.images)
            write(TopicUpdate(id = topic.id.get, title = topic.title, images = topic.images))
          }
      )
    }
  )

  private def removeUnusedImages(topicId: Long, images: List[String]): Unit = {
    Some(getTopicImagesDir(topicId, imgStorageDir).listFiles()).filterNot(_ eq null).foreach { files =>
      files.filterNot(f => images.exists(_ == f.getName))
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

  def expand = postRequest(read[List[(Long, Boolean)]]) {
    case ids =>
      val trueIds = ids.filter(_._2).map(_._1)
      val falseIds = ids.filter(!_._2).map(_._1)
      runDbActionAndCreateHttpResponse(
        DBIO.seq(
          paragraphTable.filter(_.id inSet trueIds).map(_.expanded).update(true),
          paragraphTable.filter(_.id inSet falseIds).map(_.expanded).update(false)
        ).map(_ => SharedConstants.OK)
      )
  }

  def upParagraph = changeOrder(paragraphTable, false)
  def upTopic = changeOrder(topicTable, false)
  def downParagraph = changeOrder(paragraphTable, true)
  def downTopic = changeOrder(topicTable, true)

  def addTagForTopic = formPostRequest(
    formParts = Forms.tagForm,
    onValidForm = values => runDbActionAndCreateHttpResponse(
      dao.updateField(topicTable)(values(FormKeys.PARENT_ID), _.tags)(values(FormKeys.TAG)::_).map(write(_))
    )
  )

  def removeTagFromTopic = postRequest(read[(Long, String)]){
    case (topId, tagToRemove) => runDbActionAndCreateHttpResponse(
        dao.updateField(topicTable)(topId, _.tags)(_.filterNot(_ == tagToRemove)).map(write(_))
      )
  }

  def loadParagraphsByParentId = postRequest(read[Option[Long]]) {
    case (parentParagraphIdOpt) => runDbActionAndCreateHttpResponse(
      dao.loadOrderedChildren(paragraphTable, parentParagraphIdOpt).map(write(_))
    )
  }

  def loadTopicsByParentId = postRequest(read[Long]) {
    case (parentParagraphId) => runDbActionAndCreateHttpResponse(
      dao.loadOrderedChildren(topicTable, parentParagraphId).map(write(_))
    )
  }

  private def formPostRequest(formParts: FormParts[_], onValidForm: FormValues => Future[Result]) = Action.async { request =>
    readFormDataFromPostRequest(request).values(formParts.transformations) match {
      case Right(values) => onValidForm(values)
      case Left(fd) => Future.successful(Ok(formWithErrors(fd)))
    }
  }

  private def idPostRequest = postRequest(_.toLong) _

  private def postRequest[T](parser: String => T)(action: T => Future[Result]) = Action.async { request =>
    (action compose parser) (request.body.asText.get)
  }

  private def runDbActionAndCreateHttpResponse(action: DBIOAction[String, _, _]) = db.run(
    action.asTry.map {
      case Failure(ex) => errorResponse(ex.getMessage)
      case Success(res) => dataResponse(res)
    }
  ) map {resp =>
    Ok(resp)
  }

  private def changeOrder[M <: HasIdAndOrder :ClassTag,U,C[_]](table: Query[M,U,C], down: Boolean) = idPostRequest(
    id => runDbActionAndCreateHttpResponse(
      dao.changeOrder(table, id, down).map(write(_))
    )
  )
}
