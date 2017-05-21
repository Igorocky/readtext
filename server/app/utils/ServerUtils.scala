package utils

import controllers.routes
import play.api.i18n.Lang
import play.api.mvc.BodyParsers.parse
import play.api.mvc._
import shared.forms.PostData.readPostData
import shared.forms.{FormData, FormSubmit, InputTransformation}
import shared.messages.Language
import shared.messages.Languages.{EN, RU}
import shared.pageparams.HeaderParams
import upickle.default._

import scala.concurrent.ExecutionContext

object ServerUtils {
  def getDefaultLanguage(acceptLanguages: Seq[Lang]) = acceptLanguages.toList match {
    case Nil => EN
    case locale :: tail => locale.code match {
      case "ru-RU" => RU
      case "ru" => RU
      case "en-US" => EN
      case "en" => EN
      case _ => EN
    }
  }

  def getSession(implicit requestHeader: RequestHeader): Session =
    requestHeader.session.get(Session.SESSION)
      .map(read[Session])
      .getOrElse(Session(language = getDefaultLanguage(requestHeader.acceptLanguages)))

  def modSession(request: Request[AnyContent], f: Session => Session): (String, String) = {
    (Session.SESSION -> write(f(getSession(request))))
  }

  def headerParams(language: Language) = HeaderParams (
    language = language,
    changeLanguageUrl = routes.LanguageController.changeLanguage.url
  )

  def readFormDataFromPostRequest(request: Request[AnyContent]): FormData =
    readPostData(request.body.asText.get).asInstanceOf[FormSubmit].formData
}
