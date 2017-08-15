package app.components.learncards

import app.WsClient
import app.components.WindowFunc
import app.components.listtopics.{ParTopicAttrs, TopicTree}
import japgolly.scalajs.react.{Callback, CallbackTo}
import shared.api.CardsApi
import shared.pageparams.LearnCardsPageParams

trait LearnCardsPageContext {

  //abstract members
  protected def modLearnCardsPageMem(f: LearnCardsPageMem => LearnCardsPageMem): CallbackTo[LearnCardsPageMem]
  val wsClient: WsClient[CardsApi]
  val learnCardsPageMem: LearnCardsPageMem
  val pageParams: LearnCardsPageParams
  protected def windowFunc: WindowFunc

  //actions

  def loadActiveTopics(activationTimeReduction: Option[String]) = wsClient.post(
    _.loadActiveTopics(pageParams.paragraphId, activationTimeReduction),
    windowFunc.showError
  ) {
    topics => mod(_.copy(activeTopics = TopicTree(children = Some(topics.map(topic => TopicTree(value = Some(topic)))))))
  }

  def changeActivationTimeReduction(newValue: String) = {
    val newActivationTimeReduction = if(newValue.trim.isEmpty) None else Some(newValue)
    mod(_.copy(activationTimeReduction = newActivationTimeReduction)) >> loadActiveTopics(newActivationTimeReduction)
  }

  def showTopicImgBtnClicked(topicId: Long, newValue: Option[Boolean] = None): Callback = modTopicAttribute(
    topicId,
    attrs => attrs.copy(showImg = newValue.getOrElse(!attrs.showImg))
  )

  def showTopicActions(topicId: Long, show: Boolean): Callback = modTopicAttribute(
    topicId,
    _.copy(actionsHidden = !show)
  )

  private def modTopicAttribute(topicId: Long, f: ParTopicAttrs => ParTopicAttrs) = mod(mem => mem.copy(
    activeTopics = mem.activeTopics.modNode(
      mem.activeTopics.topicSelector(topicId),
      topicNode => topicNode.changeAttrs(f)
    )
  ))


  //inner methods
  private def mod(f: LearnCardsPageMem => LearnCardsPageMem): Callback = modLearnCardsPageMem(f).void
  private def action(f: LearnCardsPageMem => Callback): Callback = modLearnCardsPageMem(m => m) >>= f
}