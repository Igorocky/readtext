package app.components.listtopics

import app.components.listtopics.GlobalScope.{NewValueChecked, NewValueExpanded}
import japgolly.scalajs.react.Callback
import shared.dto.{Paragraph, Topic}
import shared.pageparams.ListTopicsPageParams

case class GlobalScope(pageParams: ListTopicsPageParams,
                       paragraphCreated: Paragraph => Callback,
                       checkParagraphAction: (Paragraph, NewValueChecked) => Callback,
                       expandParagraphAction: (Paragraph, NewValueExpanded) => Callback,
                       checkTopicAction: (Topic, NewValueChecked) => Callback,
                       paragraphRenamed: Paragraph => Callback,
                       topicCreated: Topic => Callback,
                       topicUpdated: Topic => Callback) {

  def language = pageParams.headerParams.language
}

object GlobalScope {
  type NewValueChecked = Boolean
  type NewValueExpanded = Boolean
}