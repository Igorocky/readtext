package app.components.listtopics

import app.WsClient
import app.components.listtopics.ListTopicsPageGlobalScope.NewValueExpanded
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.api.{SessionApi, TopicApi}
import shared.dto.{Paragraph, Topic}
import shared.pageparams.ListTopicsPageParams

case class ListTopicsPageGlobalScope(pageParams: ListTopicsPageParams,
                                     registerPasteListener: (Long, File => Callback) => Callback,
                                     unregisterPasteListener: (Long) => Callback,
                                     wsClient: WsClient[TopicApi],
                                     sessionWsClient: WsClient[SessionApi],
                                     paragraphCreated: Paragraph => Callback,
                                     paragraphUpdated: Paragraph => Callback,
                                     paragraphDeleted: Long => Callback,
                                     topicCreated: Topic => Callback,
                                     topicUpdated: Topic => Callback,
                                     topicDeleted: Long => Callback,
                                     expandParagraphsAction: List[(Long, NewValueExpanded)] => Callback,
                                     moveUpParagraphAction: Long => Callback,
                                     moveUpTopicAction: Long => Callback,
                                     moveDownParagraphAction: Long => Callback,
                                     moveDownTopicAction: Long => Callback,
                                     tagAdded: (Long, List[String]) => Callback,
                                     removeTagAction: (Long, String) => Callback,
                                     filterTopic: (String, Topic) => Boolean) {

  def language = pageParams.headerParams.language
}

object ListTopicsPageGlobalScope {
  type NewValueExpanded = Boolean
}