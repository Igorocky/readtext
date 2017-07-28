package app.components.listtopics

import app.{LazyTreeNode, WsClient}
import app.components.WindowFunc
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.api.{SessionApi, TopicApi}
import shared.dto.{Paragraph, Topic}
import shared.pageparams.ListTopicsPageParams

trait ListTopicsPageContext {
  type NewValueExpanded = Boolean

  //abstract members
  protected def modListTopicsPageMem(f: ListTopicsPageMem => ListTopicsPageMem): Callback
  val wsClient: WsClient[TopicApi]
  val sessionWsClient: WsClient[SessionApi]
  val pageParams: ListTopicsPageParams
  val listTopicsPageMem: ListTopicsPageMem
  protected def windowFunc: WindowFunc

  //-----------------
  def language = pageParams.headerParams.language


  //actions
  def registerPasteListener(id: Long, listener: File => Callback): Callback = mod(_.registerListener(id, listener))

  def unregisterPasteListener(id: Long) : Callback = mod(_.unregisterPasteListener(id))

  def paragraphCreated(p: Paragraph): Callback = mod(_.addParagraph(p))
  def paragraphUpdated(p: Paragraph): Callback = mod(_.updateParagraph(p))
  def paragraphDeleted(id: Long): Callback = mod(_.deleteParagraph(id))
  def topicCreated(t: Topic): Callback = mod(_.addTopic(t))
  def topicUpdated(t: Topic): Callback = mod(_.updateTopic(t))
  def topicDeleted(id: Long): Callback = mod(_.deleteTopic(id))

  def expandParagraphsAction(ids: List[(Long, NewValueExpanded)]): Callback =
    wsClient.post(_.expand(ids), windowFunc.showError) {
      case () => mod(_.expandParagraphs(ids))
    }

  def moveUpParagraphAction(id: Long): Callback =
    wsClient.post(_.moveUpParagraph(id), windowFunc.showError) {
      case () => mod(_.moveUpParagraph(id))
    }

  def moveUpTopicAction(id: Long): Callback =
    wsClient.post(_.moveUpTopic(id), windowFunc.showError) {
      case () => mod(_.moveUpTopic(id))
    }

  def moveDownParagraphAction(id: Long): Callback =
    wsClient.post(_.moveDownParagraph(id), windowFunc.showError) {
      case () => mod(_.moveDownParagraph(id))
    }

  def moveDownTopicAction(id: Long): Callback =
    wsClient.post(_.moveDownTopic(id), windowFunc.showError) {
      case () => mod(_.moveDownTopic(id))
    }

  def tagAdded(topicId: Long, newTags: List[String]) : Callback = mod(_.setTags(topicId, newTags))

  def removeTagAction(topicId: Long, tag: String) : Callback =
    wsClient.post(_.removeTagFromTopic(topicId, tag), windowFunc.showError) {
      case tags => mod(_.setTags(topicId, tags))
    }

  def setChildren(paragraphId: Option[Long], children: List[LazyTreeNode]): Callback =
    mod(_.setChildren(paragraphId, children))

  def filterChanged(str: String) = mod(_.copy(tagFilter = str))


  //inner methods
  private def mod(f: ListTopicsPageMem => ListTopicsPageMem): Callback = modListTopicsPageMem(f)
}