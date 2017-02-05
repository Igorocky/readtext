package controllers

import javax.inject._

import css.Css
import db.Tables
import db.Tables.textTable
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
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

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

  def mergeText = Action.async { request =>
    readFormDataFromPostRequest(request).values(Forms.textForm.transformations) match {
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
}
