package app

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import shared.forms.PostData
import upickle.default.read

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Utils {
  val BTN_DEFAULT = "btn-default"
  val BTN_PRIMARY = "btn-primary"
  val BTN_SUCCESS = "btn-success"
  val BTN_INFO = "btn-info"
  val BTN_WARNING = "btn-warning"
  val BTN_DANGER = "btn-danger"
  val BTN_LINK = "btn-link"

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

  def bootstrapButton(onClick: Callback, btnType: String, tagMod: TagMod, disabled: Boolean = false) = <.button(
    ^.`type`:="button",
    ^.onClick --> onClick,
    ^.`class`:="btn " + btnType + " btn-xs",
    if (disabled) ^.disabled := true else EmptyVdom,
    tagMod
  )

  def buttonWithIcon(onClick: Callback, btnType: String, iconType: String, disabled: Boolean = false) = bootstrapButton(
    onClick = onClick,
    btnType  = btnType,
    tagMod = <.i(^.`class`:="fa " + iconType + " fa-lg"),
    disabled = disabled
  )

  def buttonWithText(onClick: Callback, btnType: String, text: String, disabled: Boolean = false) = bootstrapButton(
    onClick = onClick,
    btnType  = btnType,
    tagMod = text,
    disabled = disabled
  )

  def buttonWithImage(onClick: Callback, btnType: String, imgUrl: String, imgSize: String, disabled: Boolean = false) = bootstrapButton(
    onClick = onClick,
    btnType  = btnType,
    tagMod = <.img(
      ^.src:=imgUrl,
      ^.width:=imgSize,
      ^.height:=imgSize
    ),
    disabled = disabled
  )
}
