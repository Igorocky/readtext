package app

import app.components.SimplePage
import app.components.learncards.LearnCardsPage
import app.components.listtopics.ListTopicsPage
import japgolly.scalajs.react.component.Scala.Unmounted
import org.scalajs.dom
import shared.pageparams.{LearnCardsPageParams, ListTopicsPageParams, SimplePageParams}
import shared.{SharedConstants, StrUtils}

import scala.scalajs.js

object Main extends js.JSApp {
  private val componentMap = Map[String, String => Unmounted[_, _, _]](
    SimplePageParams.getClass.getName -> SimplePage.apply
    ,ListTopicsPageParams.getClass.getName -> ListTopicsPage.apply
    ,LearnCardsPageParams.getClass.getName -> LearnCardsPage.apply
  )

  def main(): Unit = {
    val pageType: String = getValueFromDiv(SharedConstants.PAGE_TYPE_DIV_ID)
    println("pageType = " + pageType)
    val customData: String = StrUtils.fromBytesStr(cropCdata(getValueFromDiv(SharedConstants.CUSTOM_DATA_DIV_ID)))
    println("customData = |>" + customData + "<|")
    componentMap(pageType)(customData).renderIntoDOM(dom.document.getElementById(SharedConstants.UNIV_PAGE_CONTENT_DIV_ID))
  }

  private def getValueFromDiv(id: String) = dom.document.getElementById(id).innerHTML

  private def cropCdata(str: String) = str.substring(11, str.length - 5)
}