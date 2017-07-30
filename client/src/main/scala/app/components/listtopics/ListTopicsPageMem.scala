package app.components.listtopics

import japgolly.scalajs.react.Callback
import org.scalajs.dom.raw.File

case class ListTopicsPageMem(topicTree: TopicTree = TopicTree(),
                             pasteListeners: Map[(Long, Int), File => Callback] = Map(),
                             tagFilter: String = "",
                             selectMode: Boolean = false,
                             selectedParagraphs: List[Long] = Nil,
                             selectedTopics: List[Long] = Nil,
                             selectParagraphTree: Option[TopicTree] = None) {

  def registerListener(id: Long, listener: File => Callback) = {
    val order = if (pasteListeners.isEmpty) 1 else pasteListeners.map(_._1._2).max + 1
    copy(pasteListeners = pasteListeners + ((id,order) -> listener))
  }

  def unregisterPasteListener(id: Long) = copy(pasteListeners = pasteListeners.filterNot(_._1._2 == id))


  //-------------------------

  def changeTopicTree(f: TopicTree => TopicTree): ListTopicsPageMem = copy(
    topicTree = f(topicTree)
  )
  def changeSelectParagraphTree(f: TopicTree => TopicTree): ListTopicsPageMem = copy(
    selectParagraphTree = selectParagraphTree.map(f)
  )

}
