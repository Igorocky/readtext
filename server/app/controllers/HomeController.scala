package controllers

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
import shared.messages.Languages
import shared.pageparams.{ListTextsPageParams, ListTopicsPageParams, SimplePageParams, TextUI}
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import upickle.default._
import utils.ServerUtils._

import scala.concurrent.{ExecutionContext, Future}
import scalacss.Defaults._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
                                val messagesApi: MessagesApi,
                                protected val dbConfigProvider: DatabaseConfigProvider
                              )(implicit private val environment: Environment,
                                implicit private val ec: ExecutionContext)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with Tables with I18nSupport {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def pageWithReact = Action {
    Ok(
      views.html.univpage(
        pageType = SimplePageParams.getClass.getName,
        customData = write(SimplePageParams(
          param1 = "DAFVC"
        ))
      )
    )
  }

  def changeLanguage = Action { request =>
    val newLang = Languages.fromString(request.body.asText.get)
    Ok(SharedConstants.OK).withSession(modSession(request, _.copy(language = newLang)))
  }

  def mergeText = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.textFrom.transformations, getSession(request).language) match {
      case Right(values) =>
        val text = TextUI(
          id = values(SharedConstants.ID).asInstanceOf[Option[Long]],
          title = values(SharedConstants.TITLE).asInstanceOf[String],
          content = values(SharedConstants.CONTENT).asInstanceOf[String]
        )
        if (text.id.isEmpty) {
          db.run(textTable returning textTable.map(_.id) into ((text, id) => text.copy(id = Some(id))) += text)
            .map(text => Ok(dataResponse(write(text))))
        } else {
          db.run(textTable.filter(_.id === text.id.get).update(text))
            .map(id => Ok(dataResponse(write(text))))
        }
      case Left(fd) =>
        Future.successful(Ok(formWithErrors(fd)))
    }
  }

  def allTexts = Action.async { implicit request =>
    db.run(textTable.map(r =>
      (r.id, r.title, r.content.substring(0, 200), r.content.length)
    ).sortBy(r => r._2).result).map{texts=>
      Ok(
        views.html.univpage(
          pageType = ListTextsPageParams.getClass.getName,
          customData = write(ListTextsPageParams(
            headerParams = headerParams(getSession.language),
            loadFullTextUrl = routes.HomeController.fullTextById.url,
            mergeTextUrl = routes.HomeController.mergeText.url,
            deleteTextUrl = routes.HomeController.deleteText.url,
            texts = texts.map(t => TextUI(Some(t._1), t._2, t._3 + (if (t._4 > 200) "..." else ""))).toList
          ))
        )
      )
    }
  }

  def deleteText = Action.async{ request =>
    db.run(textTable.filter(_.id === request.body.asText.get.toLong).delete).map(_ => Ok(SharedConstants.OK))
  }

  def fullTextById = Action.async {request=>
    db.run(textTable.filter(_.id === request.body.asText.get.toLong).result.head).map(text => Ok(write(text)))
  }

  def css = Action(Ok(Css.render).as("text/css"))

  def topics = Action { implicit request =>
    Ok(
      views.html.univpage(
        pageType = ListTopicsPageParams.getClass.getName,
        customData = write(ListTopicsPageParams(
          headerParams = headerParams(getSession.language),
          doActionUrl = routes.HomeController.doAction.url,
          paragraphs = List(
            Paragraph(
              id = Some(1),
              checked = false,
              name = "1.3 Functions",
              expanded = false,
              order = 0,
              topics = List(
                Topic(id = Some(1), checked = false, title = "associativity of composition", order = 0, images = Nil)
                ,Topic(id = Some(2), checked = false, title = "lemma g o f = ex", order = 1, images = Nil)
                ,Topic(id = Some(3), checked = false, title = "prop. g o f = ex && f o g = ey", order = 2, images = Nil)
                ,Topic(id = Some(4), checked = false, title = "equivalence relation", order = 3, images = Nil)
                ,Topic(id = Some(5), checked = false, title = "partial ordering", order = 4, images = Nil)
                ,Topic(id = Some(6), checked = false, title = "functional relation", order = 5, images = Nil)
                ,Topic(id = Some(7), checked = false, title = "graph of function", order = 6, images = Nil)
              )
            ),
            Paragraph(
              id = Some(2),
              checked = false,
              name = "1.4.1 The Cardinality of a Set",
              expanded = false,
              order = 1,
              topics = List(
                Topic(id = Some(8), checked = false, title = "cardX <= cardY", order = 0, images = Nil)
                ,Topic(id = Some(9), checked = false, title = "finite/infinite sets", order = 1, images = Nil)
                ,Topic(id = Some(10), checked = false, title = "linear ordering of cardinal numbers", order = 2, images = Nil)
                ,Topic(id = Some(11), checked = false, title = "cardX < cardP(X)", order = 3, images = Nil)
              )
            )

          )
        ))
      )
    )
  }

  var parId = 5
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
}
