package controllers

import javax.inject._

import css.Css
import play.api.Environment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import shared.pageparams.SimplePageParams
import slick.driver.JdbcProfile
import upickle.default._

import scala.concurrent.ExecutionContext
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

  def css = Action(Ok(Css.render).as("text/css"))
}
