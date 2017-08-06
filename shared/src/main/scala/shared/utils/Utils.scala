package shared.utils

object Utils {
  def insert[E](list: List[E], element: E)(positionSelector: (E,E) => Boolean): List[E] = list match {
    case Nil | _::Nil => list
    case x::y::rest if positionSelector(x,y) => x::element::y::rest
    case x::rest => x::insert(rest, element)(positionSelector)
  }
}
