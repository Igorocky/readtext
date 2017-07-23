package controllers

import javax.inject._

import app.RouterBuilderUtils
import shared.api.SessionApi

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionApiImpl @Inject()(implicit private val ec: ExecutionContext) extends RouterBuilderUtils[SessionApi] {

  val router: Router = RouterBuilder()

    .addHandlerWithSession(forMethod(_.changeLanguage)) {
      case (session, newLang) => Future.successful((session.copy(language = newLang), ()))
    }
}
