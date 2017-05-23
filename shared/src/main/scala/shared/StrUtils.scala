package shared

object StrUtils {
  final val UTF8 = "UTF-8"
  def toBytesStr(str: String): String = str.getBytes(UTF8).map(_.toString).mkString(",")
  def fromBytesStr(bytesStr: String): String = new String(bytesStr.split(',').map(_.toByte), UTF8)
  def listToStr(list: List[String]): String = list.mkString(";")
  def strToList(str: String): List[String] = if (str == null || str.trim == "") Nil else str.split(";").toList
}
