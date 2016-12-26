package utils

import controllers.routes
import play.api.i18n.Lang
import play.api.mvc.BodyParsers.parse
import play.api.mvc._
import shared.forms.PostData.readPostData
import shared.forms.{FormData, InputTransformation}
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

  def formOld(initialFormData: FormData,
           transformations: Map[String, InputTransformation[String, _]],
           onErrors: (FormData, Language) => Result)
          (implicit ec: ExecutionContext): BodyParser[Map[String, Any]] =
    BodyParser { requestHeader =>
      parse.anyContent(None)(requestHeader).map { resultOrBody =>
        resultOrBody.right.flatMap { body =>
          val lang = getSession(requestHeader).language
          val formData = body.asFormUrlEncoded.get
            .map{case (k,v) => (k,v.head)}
            .foldLeft(initialFormData)((fd,kv) => fd.set(kv._1, kv._2))
          formData.values(transformations, lang) match {
            case Left(formData) => Left(onErrors(formData, lang))
            case Right(values) => Right(values)
          }
        }
      }
    }

  def headerParams(language: Language) = HeaderParams (
    language = language,
    changeLanguageUrl = routes.HomeController.changeLanguage.url
  )

  def readFormDataFromPostRequest(request: Request[AnyContent]): FormData =
    read[FormData](readPostData(request.body.asText.get).content)
}
