package app.components.listtopics

import app.components.listtopics.ListTopicsPageGlobalScope.NewValueExpanded
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.pageparams.ListTopicsPageParams

case class ListTopicsPageGlobalScope(pageParams: ListTopicsPageParams,
                                     openOkDialog: String => Callback,
                                     openOkCancelDialog: (String, Callback/*onOk*/, Callback/*onCancel*/) => Callback,
                                     openOkDialog1: (String, Callback/*onOk*/) => Callback,
                                     openWaitPane: Callback,
                                     closeWaitPane: Callback,
                                     registerPasteListener: (Long, File => Callback) => Callback,
                                     unregisterPasteListener: (Long) => Callback,
                                     paragraphCreated: Paragraph => Callback,
                                     paragraphUpdated: ParagraphUpdate => Callback,
                                     paragraphDeleted: Long => Callback,
                                     topicCreated: Topic => Callback,
                                     topicUpdated: TopicUpdate => Callback,
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