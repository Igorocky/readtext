package controllers

import java.io.File
import javax.inject._

import db.Tables
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.SharedConstants
import shared.dto.{Paragraph, Topic}
import shared.forms.Forms
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
          paragraphs = ps
//          paragraphs = List(
//            Paragraph(
//              id = Some(1),
//              checked = false,
//              name = "1.3 Functions",
//              expanded = false,
//              order = 0,
//              topics = List(
//                Topic(id = Some(1), checked = false, title = "associativity of composition", order = 0, images = Nil)
//                ,Topic(id = Some(2), checked = false, title = "lemma g o f = ex", order = 1, images = Nil)
//                ,Topic(id = Some(3), checked = false, title = "prop. g o f = ex && f o g = ey", order = 2, images = Nil)
//                ,Topic(id = Some(4), checked = false, title = "equivalence relation", order = 3, images = Nil)
//                ,Topic(id = Some(5), checked = false, title = "partial ordering", order = 4, images = Nil)
//                ,Topic(id = Some(6), checked = false, title = "functional relation", order = 5, images = Nil)
//                ,Topic(id = Some(7), checked = false, title = "graph of function", order = 6, images = Nil)
//              )
//            ),
//            Paragraph(
//              id = Some(2),
//              checked = false,
//              name = "1.4.1 The Cardinality of a Set",
//              expanded = false,
//              order = 1,
//              topics = List(
//                Topic(id = Some(8), checked = false, title = "cardX <= cardY", order = 0, images = Nil)
//                ,Topic(id = Some(9), checked = false, title = "finite/infinite sets", order = 1, images = Nil)
//                ,Topic(id = Some(10), checked = false, title = "linear ordering of cardinal numbers", order = 2, images = Nil)
//                ,Topic(id = Some(11), checked = false, title = "cardX < cardP(X)", order = 3, images = Nil)
//              )
//            )
//
//          )
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
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations, getSession(request).language) match {
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
    readFormDataFromPostRequest(request).values(Forms.paragraphForm.transformations, getSession(request).language) match {
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
    readFormDataFromPostRequest(request).values(Forms.topicForm.transformations, getSession(request).language) match {
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
    readFormDataFromPostRequest(request).values(Forms.topicForm.transformations, getSession(request).language) match {
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

  def uploadFile(fileName: String) = Action(parse.multipartFormData) { request =>
    println(">in upload")
    request.body.files.foreach(f => println(s"file from sequence, key = ${f.key}, filename = ${f.filename}"))
    request.body.file("fileQQWWEE").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(new File(s"D:\\temp\\uploaded\\$filename"))
      Ok("File uploaded")
    }.getOrElse {
      Ok("File was not uploaded")
    }
  }
}
