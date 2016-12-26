package app

import japgolly.scalajs.react.Callback
import org.scalajs.dom

object Utils {
  def navigateTo(url: String) = Callback {
    dom.window.location.href = url
  }
}
