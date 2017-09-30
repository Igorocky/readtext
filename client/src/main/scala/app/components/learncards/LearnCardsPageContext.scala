package app.components.learncards

import app.WsClient
import app.components.WindowFunc
import app.components.listtopics._
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom.raw.File
import shared.api.{CardsApi, TopicApi}
import shared.dto.Topic
import shared.pageparams.LearnCardsPageParams

trait LearnCardsPageContext extends TopicCmpActions with ScoreCmpActions with TopicActionsCmpActions with ImgUploaderActions {

  //abstract members
  protected def modLearnCardsPageMem(f: LearnCardsPageMem => LearnCardsPageMem): CallbackTo[LearnCardsPageMem]
  val cardsApi: WsClient[CardsApi]
  val topicApi: WsClient[TopicApi]
  val learnCardsPageMem: LearnCardsPageMem
  val pageParams: LearnCardsPageParams
  protected def windowFunc: WindowFunc

  //actions

  def loadTopics(activationTimeReduction: Option[String]) = cardsApi.post(
    _.loadActiveTopics(pageParams.paragraphId, activationTimeReduction),
    windowFunc.showError
  ) {activeTopics =>
    cardsApi.post(_.loadNewTopics(pageParams.paragraphId), windowFunc.showError) { newTopics =>
      mod(_.copy(
        activeTopics = TopicTree(children = Some(activeTopics.map(topic => TopicTree(value = Some(topic))))),
        newTopics = TopicTree(children = Some(newTopics.map(topic => TopicTree(value = Some(topic)))))
      ))
    }
  }

  def changeActivationTimeReduction(newValue: String) = {
    val newActivationTimeReduction = if(newValue.trim.isEmpty) None else Some(newValue)
    mod(_.copy(activationTimeReduction = newActivationTimeReduction)) >> loadTopics(newActivationTimeReduction)
  }

  def showTopicImgBtnClicked2(topicId: Long, newValue: Option[Boolean] = None): Callback = modTopicAttribute(
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
  )) >> mod(mem => mem.copy(
    newTopics = mem.newTopics.modNode(
      mem.newTopics.topicSelector(topicId),
      topicNode => topicNode.changeAttrs(f)
    )
  ))

  override def wf = windowFunc
  override def cardStateUpdated(cardId: Long): CallbackTo[Unit] =
    loadTopics(learnCardsPageMem.activationTimeReduction)

  override def changeTopicSelection(topicId: Long, selected: Boolean) = Callback.empty

  override def showTopicImgBtnClicked(topicId: Long) = showTopicImgBtnClicked2(topicId)

  override def moveUpTopicAction(topicId: Long) = Callback.empty

  override def moveDownTopicAction(topicId: Long) = Callback.empty

  override def topicDeleted(topicId: Long) = Callback.empty

  override def topicUpdated(topic: Topic) = Callback.empty

  override def unregisterPasteListener(listenerId: Long) = Callback.empty

  override def registerPasteListener(listenerId: Long, listener: (File) => Callback) = Callback.empty

  //inner methods
  private def mod(f: LearnCardsPageMem => LearnCardsPageMem): Callback = modLearnCardsPageMem(f).void
  private def action(f: LearnCardsPageMem => Callback): Callback = modLearnCardsPageMem(m => m) >>= f
}