package app.components.listtopics

import app.WsClient
import shared.api.{SessionApi, TopicApi}
import shared.dto.Topic
import shared.pageparams.ListTopicsPageParams

case class ListTopicsPageGlobalScope(pageParams: ListTopicsPageParams,
                                     wsClient: WsClient[TopicApi],
                                     sessionWsClient: WsClient[SessionApi],
                                     filterTopic: (String, Topic) => Boolean
                                     ) {

  def language = pageParams.headerParams.language
}

object ListTopicsPageGlobalScope {
  type NewValueExpanded = Boolean
}