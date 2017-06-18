package shared.utils

import scala.collection.mutable.ListBuffer

trait Enum[E] {
  private var elems = ListBuffer[E]()
  private var isFull = false
  private lazy val isFullLazy = isFull

  lazy val allElems: List[E] =
    if (!isFullLazy) throw new Exception(s"Building of enum ${this.getClass} was not finished")
    else {
      val res = elems.result()
      elems = null
      res
    }

  protected def addElem[E1 <: E](elem: E1): E1 = {
    elems += elem
    elem
  }

  def end: Unit = isFull = true
}
