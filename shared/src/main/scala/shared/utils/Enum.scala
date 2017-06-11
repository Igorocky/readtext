package shared.utils

import scala.collection.mutable.ListBuffer

trait Enum[E] {
  private val elems = ListBuffer[E]()
  private var isFull = false
  private lazy val isFullLazy = isFull

  lazy val allElems: List[E] = if (!isFullLazy) ??? else elems.toList

  protected def addElem(elem: E): E = {
    elems += elem
    elem
  }

  def end: Unit = isFull = true
}
