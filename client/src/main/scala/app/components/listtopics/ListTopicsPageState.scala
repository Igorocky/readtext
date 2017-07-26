package app.components.listtopics

import app.components.{WindowFunc, WindowFuncMem}
import app.{LazyTreeNode, WsClient}
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.api.{SessionApi, TopicApi}
import shared.dto.{Paragraph, Topic}
import shared.messages.Language
import shared.pageparams.ListTopicsPageParams

case class ListTopicsPageState(modState: (ListTopicsPageState => ListTopicsPageState) => Callback = null,
                               windowFuncMem: WindowFuncMem = WindowFuncMem(),
                               listTopicsPageMem: ListTopicsPageMem = ListTopicsPageMem(),
                               wsClient: WsClient[TopicApi] = null,
                               sessionWsClient: WsClient[SessionApi] = null,
                               pageParams: ListTopicsPageParams) extends WindowFunc with ListTopicsPageContext {

  override protected def modWindowFuncMem(f: WindowFuncMem => WindowFuncMem): Callback =
    modState(s => s.copy(windowFuncMem = f(s.windowFuncMem)))

  override protected def modListTopicsPageMem(f: (ListTopicsPageMem) => ListTopicsPageMem): Callback =
    modState(s => s.copy(listTopicsPageMem = f(s.listTopicsPageMem)))

  override protected def windowFunc: WindowFunc = this

  def runPasteListener(file: File): Unit =
    if (listTopicsPageMem.pasteListeners.isEmpty) ()
    else listTopicsPageMem.pasteListeners.maxBy{case ((_, order), _) => order}._2(file).runNow()

  def setLanguage(language: Language): ListTopicsPageState =
    copy(
      pageParams = pageParams.copy(
        headerParams = pageParams.headerParams.copy(
          language = language
        )
      )
    )
}

case class ListTopicsPageMem(data: LazyTreeNode = LazyTreeNode(),
                             pasteListeners: Map[(Long, Int), File => Callback] = Map(),
                             tagFilter: String = "") {

  def registerListener(id: Long, listener: File => Callback) = {
    val order = if (pasteListeners.isEmpty) 1 else pasteListeners.map(_._1._2).max + 1
    copy(pasteListeners = pasteListeners + ((id,order) -> listener))
  }

  def unregisterPasteListener(id: Long) = copy(pasteListeners = pasteListeners.filterNot(_._1._2 == id))

  def addParagraph(paragraph: Paragraph) = changeData(_.addChild(
    paragraphSelector(paragraph.paragraphId),
    LazyTreeNode(Some(paragraph), None)
  ))

  def updateParagraph(par: Paragraph) = changeData(_.updateValue(
    paragraphSelector(par.id),
    _.map(_.asInstanceOf[Paragraph].copy(name = par.name))
  ))

  def deleteParagraph(id: Long) = changeData(_.removeNode(paragraphSelector(id)))

  def addTopic(topic: Topic) = changeData(_.addChild(
    paragraphSelector(topic.paragraphId),
    LazyTreeNode(Some(topic), None)
  ))

  def updateTopic(topic: Topic) = changeData(_.updateValue(
    topicSelector(topic.id),
    _.map(_.asInstanceOf[Topic].copy(title = topic.title, images = topic.images))
  ))

  def deleteTopic(topId: Long) = changeData(_.removeNode(topicSelector(topId)))

  def expandParagraph(id: Long, newExpanded: Boolean) = changeData(_.updateValue(
    paragraphSelector(id),
    _.map(_.asInstanceOf[Paragraph].copy(expanded = newExpanded))
  ))

  def expandParagraphs(ids: List[(Long, Boolean)]) =
    ids.foldLeft(this){case (s,(id, expanded)) => s.expandParagraph(id, expanded)}

  def moveUpTopic(id: Long) = changeData(_.moveUp(topicSelector(id)))

  def moveUpParagraph(id: Long) = changeData(_.moveUp(paragraphSelector(id)))

  def moveDownParagraph(id: Long) = changeData(_.moveDown(paragraphSelector(id)))

  def moveDownTopic(id: Long) = changeData(_.moveDown(topicSelector(id)))

  def setTags(topicId: Long, tags: List[String]) = changeData(_.updateValue(
    topicSelector(topicId),
    _.map(_.asInstanceOf[Topic].copy(tags = tags))
  ))

  def setChildren(paragraphId: Option[Long], children: List[LazyTreeNode]) = changeData(_.setChildren(
    paragraphSelector(paragraphId),
    children
  ))

  //-------------------------

  private def changeData(f: LazyTreeNode => LazyTreeNode) = copy(data = f(data))

  private def paragraphSelector(idOpt: Option[Long]): LazyTreeNode => Boolean =
    if (idOpt.isEmpty) {
      node => {
        node match {
          case LazyTreeNode(None, _) => true
          case _ => false
        }
      }
    } else {
      node => {
        node match {
          case LazyTreeNode(Some(p: Paragraph), _) if p.id == idOpt => true
          case _ => false
        }
      }
    }

  private def paragraphSelector(id: Long): LazyTreeNode => Boolean = paragraphSelector(Some(id))

  private def topicSelector(idOpt: Option[Long]): LazyTreeNode => Boolean =
    node => node match {
      case LazyTreeNode(Some(t: Topic), _) if t.id == idOpt => true
      case _ => false
    }

  private def topicSelector(id: Long): LazyTreeNode => Boolean = topicSelector(Some(id))
}

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