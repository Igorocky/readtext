package app

import org.scalajs.dom.ClipboardEvent
import org.scalajs.dom.raw.File

import scala.scalajs.js

@js.native
object JsGlobalScope extends js.GlobalScope{
  def extractFileFromEvent(e: ClipboardEvent): File = js.native
}
