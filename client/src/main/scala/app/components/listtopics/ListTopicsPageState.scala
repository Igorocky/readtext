package app.components.listtopics

import app.LazyTreeNode
import app.components.{WindowFunc, WindowFuncMem}
import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File
import shared.dto.{Paragraph, Topic}
import shared.messages.Language

case class ListTopicsPageState(modState: (ListTopicsPageState => ListTopicsPageState) => Callback = null,
                               windowFuncMem: WindowFuncMem = WindowFuncMem(),
                               globalScope: ListTopicsPageGlobalScope = null,
                               data: LazyTreeNode = LazyTreeNode(),
                               pasteListeners: Map[(Long, Int), File => Callback] = Map(),
                               tagFilter: String = "") extends WindowFunc {

  override protected def modWindowFuncMem(f: WindowFuncMem => WindowFuncMem): Callback =
    modState(s => s.copy(windowFuncMem = f(s.windowFuncMem)))

  def registerListener(id: Long, listener: File => Callback): ListTopicsPageState = {
    val order = if (pasteListeners.isEmpty) 1 else pasteListeners.map(_._1._2).max + 1
    copy(pasteListeners = pasteListeners + ((id,order) -> listener))
  }

  def runPasteListener(file: File): Unit =
    if (pasteListeners.isEmpty) () else pasteListeners.maxBy(_._1._2)._2(file).runNow()

  def setLanguage(language: Language): ListTopicsPageState = copy(
    globalScope = globalScope.copy(
      pageParams = globalScope.pageParams.copy(
        headerParams = globalScope.pageParams.headerParams.copy(
          language = language
        )
      )
    )
  )

  def setGlobalScope(globalScope: ListTopicsPageGlobalScope): ListTopicsPageState = copy(globalScope = globalScope)

  def addParagraph(paragraph: Paragraph): ListTopicsPageState = changeData(_.addChild(
    paragraphSelector(paragraph.paragraphId),
    LazyTreeNode(Some(paragraph), None)
  ))

  def updateParagraph(par: Paragraph): ListTopicsPageState = changeData(_.updateValue(
    paragraphSelector(par.id),
    _.map(_.asInstanceOf[Paragraph].copy(name = par.name))
  ))

  def deleteParagraph(id: Long): ListTopicsPageState = changeData(_.removeNode(paragraphSelector(id)))

  def addTopic(topic: Topic): ListTopicsPageState = changeData(_.addChild(
    paragraphSelector(topic.paragraphId),
    LazyTreeNode(Some(topic), None)
  ))

  def updateTopic(topic: Topic): ListTopicsPageState = changeData(_.updateValue(
    topicSelector(topic.id),
    _.map(_.asInstanceOf[Topic].copy(title = topic.title, images = topic.images))
  ))

  def deleteTopic(topId: Long) = changeData(_.removeNode(topicSelector(topId)))

  def expandParagraph(id: Long, newExpanded: Boolean): ListTopicsPageState = changeData(_.updateValue(
    paragraphSelector(id),
    _.map(_.asInstanceOf[Paragraph].copy(expanded = newExpanded))
  ))

  def expandParagraphs(ids: List[(Long, Boolean)]): ListTopicsPageState =
    ids.foldLeft(this){case (s,(id, expanded)) => s.expandParagraph(id, expanded)}

  def moveUpTopic(id: Long): ListTopicsPageState = changeData(_.moveUp(topicSelector(id)))

  def moveUpParagraph(id: Long): ListTopicsPageState = changeData(_.moveUp(paragraphSelector(id)))

  def moveDownParagraph(id: Long): ListTopicsPageState = changeData(_.moveDown(paragraphSelector(id)))

  def moveDownTopic(id: Long): ListTopicsPageState = changeData(_.moveDown(topicSelector(id)))

  def setTags(topicId: Long, tags: List[String]): ListTopicsPageState = changeData(_.updateValue(
    topicSelector(topicId),
    _.map(_.asInstanceOf[Topic].copy(tags = tags))
  ))

  def setChildren(paragraphId: Option[Long], children: List[LazyTreeNode]): ListTopicsPageState = changeData(_.setChildren(
    paragraphSelector(paragraphId),
    children
  ))

  //-------------------------

  private def changeData(f: LazyTreeNode => LazyTreeNode): ListTopicsPageState = copy(data = f(data))

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
