package app.components.listtopics

import app.components.listtopics.GlobalScope.{NewValueChecked, NewValueExpanded}
import japgolly.scalajs.react.Callback
import shared.dto.{Paragraph, ParagraphUpdate, Topic, TopicUpdate}
import shared.pageparams.ListTopicsPageParams

case class GlobalScope(pageParams: ListTopicsPageParams,
                       openOkDialog: String => Callback,
                       openOkCancelDialog: (String, Callback/*onOk*/, Callback/*onCancel*/) => Callback,
                       openOkCancelDialog1: (String, Callback/*onOk*/) => Callback,
                       openWaitPane: Callback,
                       closeWaitPane: Callback,
                       paragraphCreated: Paragraph => Callback,
                       paragraphUpdated: ParagraphUpdate => Callback,
                       paragraphDeleted: Long => Callback,
                       topicCreated: Topic => Callback,
                       topicUpdated: TopicUpdate => Callback,
                       topicDeleted: Long => Callback,
                       checkParagraphAction: (Paragraph, NewValueChecked) => Callback,
                       expandParagraphAction: (Paragraph, NewValueExpanded) => Callback,
                       checkTopicAction: (Topic, NewValueChecked) => Callback) {

  def language = pageParams.headerParams.language
}

object GlobalScope {
  type NewValueChecked = Boolean
  type NewValueExpanded = Boolean
}