package readtext

import scala.collection.mutable.ListBuffer

trait Enum[E] {
  private val elems = ListBuffer[E]()
  private var fixed = false
  lazy val allElems = {
    fixed = true
    elems.toList
  }

  protected def addElem(elem: E) = {
    if (fixed) {
      ???
    } else {
      elems += elem
      elem
    }
  }
}
