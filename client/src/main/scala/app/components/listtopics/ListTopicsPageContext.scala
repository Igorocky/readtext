package app.components.listtopics

import app.WsClient
import app.components.WindowFunc
import japgolly.scalajs.react.{Callback, CallbackTo}
import org.scalajs.dom.raw.File
import shared.api.{SessionApi, TopicApi}
import shared.dto.{Paragraph, Topic}
import shared.pageparams.ListTopicsPageParams

trait ListTopicsPageContext {
  type NewValueExpanded = Boolean

  //abstract members
  protected def modListTopicsPageMem(f: ListTopicsPageMem => ListTopicsPageMem): CallbackTo[ListTopicsPageMem]
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

  def paragraphCreated(p: Paragraph): Callback = mod(_.changeTopicTree(_.addParagraph(p)))
  def paragraphUpdated(p: Paragraph): Callback = mod(_.changeTopicTree(_.updateParagraph(p)))
  def paragraphDeleted(id: Long): Callback = mod(_.changeTopicTree(_.deleteParagraph(id)))
  def topicCreated(t: Topic): Callback = mod(_.changeTopicTree(_.addTopic(t)))
  def topicUpdated(t: Topic): Callback = mod(_.changeTopicTree(_.updateTopic(t)))
  def topicDeleted(id: Long): Callback = mod(_.changeTopicTree(_.deleteTopic(id)))

  def expandParagraphsAction(ids: List[(Long, NewValueExpanded)]): Callback =
    wsClient.post(_.expand(ids), windowFunc.showError) {
      case () => mod(_.changeTopicTree(_.expandParagraphs(ids)))
    }

  def collapseAllAction = action{mem=>
    val expandedParagraphs = mem.topicTree.findNodes{
      case TopicTree(Some(p:Paragraph), _, _) if p.expanded => true
      case _ => false
    }.map(_.value.get.asInstanceOf[Paragraph])
    expandParagraphsAction(expandedParagraphs.map(p => (p.id.get, false)))
  }

  def moveUpParagraphAction(id: Long): Callback =
    wsClient.post(_.moveUpParagraph(id), windowFunc.showError) {
      case () => mod(_.changeTopicTree(_.moveUpParagraph(id)))
    }

  def moveUpTopicAction(id: Long): Callback =
    wsClient.post(_.moveUpTopic(id), windowFunc.showError) {
      case () => mod(_.changeTopicTree(_.moveUpTopic(id)))
    }

  def moveDownParagraphAction(id: Long): Callback =
    wsClient.post(_.moveDownParagraph(id), windowFunc.showError) {
      case () => mod(_.changeTopicTree(_.moveDownParagraph(id)))
    }

  def moveDownTopicAction(id: Long): Callback =
    wsClient.post(_.moveDownTopic(id), windowFunc.showError) {
      case () => mod(_.changeTopicTree(_.moveDownTopic(id)))
    }

  def tagAdded(topicId: Long, newTags: List[String]) : Callback = mod(_.changeTopicTree(_.setTags(topicId, newTags)))

  def removeTagAction(topicId: Long, tag: String) : Callback =
    wsClient.post(_.removeTagFromTopic(topicId, tag), windowFunc.showError) {
      case tags => mod(_.changeTopicTree(_.setTags(topicId, tags)))
    }

  def setChildrenOfMainTopicTree(paragraphId: Option[Long], children: List[TopicTree]): Callback =
    mod(_.changeTopicTree(_.setChildren(paragraphId, children)))

  def setChildrenOfSelectParagraphTree(paragraphId: Option[Long], children: List[TopicTree]): Callback =
    mod(_.changeSelectParagraphTree(_.setChildren(paragraphId, children)))

  def filterChanged(str: String) = mod(_.copy(tagFilter = str))

  def loadChildrenIntoMainTopicTree(paragraphId: Option[Long]): Callback = {
    def setChildren(children: List[Any]) =
      setChildrenOfMainTopicTree(paragraphId, children.map(c => TopicTree(Some(c), None)))

    windowFunc.openWaitPane >> wsClient.post(_.loadParagraphsByParentId(paragraphId), _ => windowFunc.openOkDialog("Error loading paragraphs"))(
      paragraphs => if (paragraphId.isDefined) {
        wsClient.post(_.loadTopicsByParentId(paragraphId.get), _ => windowFunc.openOkDialog("Error loading topics"))(
          topics => setChildren(paragraphs ::: topics) >> windowFunc.closeWaitPane
        )
      } else {
        setChildren(paragraphs) >> windowFunc.closeWaitPane
      }
    )
  }

  def loadChildrenIntoSelectParagraphTree(paragraphId: Option[Long]): Callback = {
    def setChildren(children: List[Any]) =
      setChildrenOfSelectParagraphTree(paragraphId, children.map(c => TopicTree(Some(c), None)))

    windowFunc.openWaitPane >> wsClient.post(_.loadParagraphsByParentId(paragraphId), _ => windowFunc.openOkDialog("Error loading paragraphs"))(
      paragraphs => setChildren(paragraphs) >> windowFunc.closeWaitPane
    )
  }

  def gotoSelectMode = mod(m => m.copy(selectMode = true, topicTree = m.topicTree.modNode(_.selected, _.copy(selected = false))))
  def cancelSelectMode = closeSelectParagraphWindow >> mod(_.copy(selectMode = false))
  def selectTopicAction(id: Long, selected: Boolean): Callback = mod(_.changeTopicTree(_.selectTopic(id, selected)))
  def selectParagraphAction(id: Long, selected: Boolean): Callback = mod(_.changeTopicTree(_.selectParagraph(Some(id), selected)))
  def selectParagraphInDialogAction(id: Option[Long]): Callback = mod(_.changeSelectParagraphTree(
    _.modNode(_.selected, _.copy(selected = false)).selectParagraph(id, true)
  ))
  def openSelectParagraphWindow = mod(mem => mem.copy(
    selectedParagraphs = mem.topicTree.findNodes {
      case TopicTree(Some(p: Paragraph), _, true) => true
      case _ => false
    }.map(_.value.get.asInstanceOf[Paragraph].id.get),
    selectedTopics = mem.topicTree.findNodes {
      case TopicTree(Some(t: Topic), _, true) => true
      case _ => false
    }.map(_.value.get.asInstanceOf[Topic].id.get)
  )) >> mod(mem => mem.copy(
    selectParagraphTree =
      Some(TopicTree(value = (if (mem.selectedTopics.nonEmpty) None else Some(Paragraph(name = "ROOT")))))
  ))
  def closeSelectParagraphWindow = mod(_.copy(selectParagraphTree = None, selectedParagraphs = Nil))
  def moveSelectedItems = action{mem=>
    val destParId = mem.selectParagraphTree.get.findNodes(_.selected).head
      .value.get.asInstanceOf[Paragraph].id
    windowFunc.openWaitPane >>
      wsClient.post(_.changeParagraphsParent(mem.selectedParagraphs, destParId), windowFunc.showError) {_=>
        mem.selectedParagraphs.foldLeft(Callback.empty) {
          case (cb, parIdToBeRelocated) => cb >> mod(_.changeTopicTree(_.relocateParagraph(parIdToBeRelocated, destParId)))
        } >>
          (if (mem.selectedTopics.nonEmpty) {
            wsClient.post(_.changeTopicsParent(mem.selectedTopics, destParId.get), windowFunc.showError) { _ =>
              mem.selectedTopics.foldLeft(Callback.empty) {
                case (cb, topIdToBeRelocated) => cb >> mod(_.changeTopicTree(_.relocateTopic(topIdToBeRelocated, destParId.get)))
              }
            }
          } else {
            Callback.empty
          }) >> cancelSelectMode >> windowFunc.closeWaitPane
      }
  }

  //inner methods
  private def mod(f: ListTopicsPageMem => ListTopicsPageMem): Callback = modListTopicsPageMem(f).void
  private def action(f: ListTopicsPageMem => Callback): Callback = modListTopicsPageMem(m => m) >>= f
}