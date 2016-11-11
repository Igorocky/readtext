package example

import example.components.SimplePage
import japgolly.scalajs.react.{ReactDOM, ReactElement}
import org.scalajs.dom
import shared.SharedConstants
import shared.pageparams.SimplePageParams

import scala.scalajs.js

object Main extends js.JSApp {
  private val componentMap = Map[String, String => ReactElement](
    SimplePageParams.getClass.getName -> SimplePage.apply
  )

  def main(): Unit = {
    val pageType: String = getValueFromDiv(SharedConstants.PAGE_TYPE_DIV_ID)
    println("pageType = " + pageType)
    val customData: String = getValueFromDiv(SharedConstants.CUSTOM_DATA_DIV_ID)
    println("customData = " + customData)
    ReactDOM.render(
      componentMap(pageType)(customData),
      dom.document.getElementById(SharedConstants.UNIV_PAGE_CONTENT_DIV_ID)
    )
  }

  private def getValueFromDiv(id: String) = dom.document.getElementById(id).innerHTML
}