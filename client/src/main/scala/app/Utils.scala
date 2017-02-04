package app

import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import shared.forms.PostData
import upickle.default.read

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Utils {
  def navigateTo(url: String) = Callback {
    dom.window.location.href = url
  }

  def post[T](url: String, data: Ajax.InputData)(f: Try[PostData] => CallbackTo[T]): CallbackTo[Future[T]] =
    CallbackTo.future {
      Ajax.post(url = url, data = data).map {
        r => Success(read[PostData](r.responseText))
      }.recover[Try[PostData]] {
        case throwable =>
          Failure(throwable)
      }.map(f)
    }
}
