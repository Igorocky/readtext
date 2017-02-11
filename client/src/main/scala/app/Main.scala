package app

import app.components.listtopics.ListTopicsPage
import app.components.{ListTextsPage, SimplePage}
import japgolly.scalajs.react.{ReactDOM, ReactElement}
import org.scalajs.dom
import shared.{SharedConstants, StrUtils}
import shared.pageparams.{ListTextsPageParams, ListTopicsPageParams, SimplePageParams}

import scala.scalajs.js

object Main extends js.JSApp {
  private val componentMap = Map[String, String => ReactElement](
    SimplePageParams.getClass.getName -> SimplePage.apply
    ,ListTextsPageParams.getClass.getName -> ListTextsPage.apply
    ,ListTopicsPageParams.getClass.getName -> ListTopicsPage.apply
  )

  def main(): Unit = {
    val pageType: String = getValueFromDiv(SharedConstants.PAGE_TYPE_DIV_ID)
    println("pageType = " + pageType)
    val customData: String = StrUtils.fromBytesStr(cropCdata(getValueFromDiv(SharedConstants.CUSTOM_DATA_DIV_ID)))
    println("customData = |>" + customData + "<|")
    ReactDOM.render(
      componentMap(pageType)(customData),
      dom.document.getElementById(SharedConstants.UNIV_PAGE_CONTENT_DIV_ID)
    )
  }

  private def getValueFromDiv(id: String) = dom.document.getElementById(id).innerHTML

  private def cropCdata(str: String) = str.substring(11, str.length - 5)
}