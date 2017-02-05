package controllers

import javax.inject._

import db.Tables
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.SharedConstants
import shared.messages.Languages
import slick.driver.JdbcProfile
import utils.ServerUtils._

import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class LanguageController @Inject()(
                                val messagesApi: MessagesApi,
                                protected val dbConfigProvider: DatabaseConfigProvider
                              )(implicit private val environment: Environment,
                                implicit private val ec: ExecutionContext)
  extends Controller with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  def changeLanguage = Action { request =>
    val newLang = Languages.fromString(request.body.asText.get)
    Ok(SharedConstants.OK).withSession(modSession(request, _.copy(language = newLang)))
  }
}
