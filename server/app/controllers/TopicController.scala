package controllers

import java.io.File
import javax.inject._

import db.{DaoCommon, PrintSchema}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Environment, Logger}
import shared.SharedConstants._
import shared.pageparams.{LearnCardsPageParams, ListTopicsPageParams}
import slick.jdbc.JdbcProfile
import upickle.default._
import utils.ServerUtils._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TopicController @Inject()(
                                val messagesApi: MessagesApi,
                                protected val dbConfigProvider: DatabaseConfigProvider,
                                val configuration: play.api.Configuration,
                                val dao: DaoCommon,
                                val wsRouter: Router
                              )(implicit private val environment: Environment,
                                implicit private val ec: ExecutionContext)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  new PrintSchema

  val getTopicImgUrl = """/\w+""".r.findFirstIn(routes.TopicController.topicImg(1,"").url).get
  val learnTopicsUrl = """/\w+""".r.findFirstIn(routes.TopicController.learnTopics(1).url).get
  def topics = Action { implicit request =>
    Ok(
      views.html.univpage(
        pageType = ListTopicsPageParams.getClass.getName,
        customData = write(ListTopicsPageParams(
          headerParams = headerParams(getSession.language),
          uploadTopicFileUrl = routes.TopicController.uploadTopicImage.url,
          getTopicImgUrl = getTopicImgUrl,
          learnTopicsUrl = learnTopicsUrl,
          wsEntryUrl = routes.TopicController.wsEntry.url
        )),
        pageTitle = "Topics"
      )
    )
  }

  def learnTopics(paragraphId: Long) = Action { implicit request =>
    Ok(
      views.html.univpage(
        pageType = LearnCardsPageParams.getClass.getName,
        customData = write(LearnCardsPageParams(
          headerParams = headerParams(getSession.language),
          wsEntryUrl = routes.TopicController.wsEntry.url,
          paragraphId = paragraphId,
          getTopicImgUrl = getTopicImgUrl
        )),
        pageTitle = "Learn Cards"
      )
    )
  }

  def wsEntry = postRequest(read[(String, String)]) {
    case (session, (path, input)) =>
      Logger.debug(s"wsEntry.input: path: '$path', input: '$input'")
      wsRouter.handle(path, session, input)
        .map(_.map{case (ses, res) =>
          Logger.debug(s"wsEntry.output: path: '$path', input: '$input', result: '$res'")
          Ok(res).withSession(modSession(ses))
        }).getOrElse {
          val msg = s"No handler found for path '$path'"
          Logger.error(msg)
          Future.successful(BadRequest(msg))
        }
  }

  private val imgStorageDir = new File(configuration.getString("topicsImgStorageDir").get)
  def uploadTopicImage = Action(parse.multipartFormData) { request =>
    request.body.file(FILE).map { file =>
      val topicId = request.body.dataParts(TOPIC_ID)(0).toLong
      val topicFilesDir = getTopicImagesDir(topicId, imgStorageDir)
      val topicFileName = generateNameForNewFile(topicFilesDir) + getFileExtension(file.filename)
      file.ref.moveTo(new File(topicFilesDir, topicFileName))
      Ok(write[Either[String,String]](Right(topicFileName)))
    }.getOrElse {
      Ok(write[Either[String,String]](Left("Could not upload file.")))
    }
  }

  private def getFileExtension(name: String): String = if (name.contains('.')) '.' + name.split('.').last else ""

  private def getFileNameWithoutExtension(name: String): String =
    if (name.contains('.')) name.substring(0, name.lastIndexOf('.')) else name

  private def getTopicImagesDir(topicId: Long, imgStorageDir: File): File =
    new File(imgStorageDir, s"/$topicId")

  private def generateNameForNewFile(topicFilesDir: File): String = {
    topicFilesDir.mkdirs()
    val existingFileNames = topicFilesDir.listFiles().view
      .map(_.getName)
      .map(getFileNameWithoutExtension)
      .filter(_.forall(_.isDigit))
      .map(_.toLong)
    val res = if (existingFileNames.isEmpty) 0 else existingFileNames.max + 1
    res.toString
  }

  private val fileNamePattern = """^\d+(\.\w+)?$""".r
  def topicImg(topicId: Long, fileName: String) = Action {
    println(s"fileName = ${fileName}")
    fileName match {
      case fileNamePattern(_) => Ok.sendFile(new File(imgStorageDir + "/" + topicId + "/" + fileName))
      case _ => NotFound(s"File $fileName was not found.")
    }
  }

  private def postRequest[T](parser: String => T)(action: (utils.Session, T) => Future[Result]) = Action.async {implicit request =>
    action(getSession, parser(request.body.asText.get))
  }
}
