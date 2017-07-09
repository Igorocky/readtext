package readtext

case class ReadingPhase(name: String)
object ReadingPhase extends shared.utils.Enum[ReadingPhase] {
  private def addElem(str: String) = ReadingPhase(str)
  val ONLY_TEXT = addElem("ONLY_TEXT")
  val TEXT_WITH_INPUTS = addElem("TEXT_WITH_INPUTS")
}