package shared

import shared.forms.FormKey

object FormKeys {
  final val ID = optLongKey("ID")
  final val PARENT_ID = longKey("ID")
  final val PARAGRAPH_ID = optLongKey("PARAGRAPH_ID")
  final val TITLE = stringKey("TITLE")
  final val CONTENT = stringKey("CONTENT")
  final val IMAGES = listOfStringsKey("IMAGES")
  final val TAG = stringKey("TAG")


  private def optLongKey(namePar: String) = new FormKey {
    override type ValueType = Option[Long]
    override val name: String = namePar
  }

  private def longKey(namePar: String) = new FormKey {
    override type ValueType = Long
    override val name: String = namePar
  }

  private def stringKey(namePar: String) = new FormKey {
    override type ValueType = String
    override val name: String = namePar
  }

  private def listOfStringsKey(namePar: String) = new FormKey {
    override type ValueType = List[String]
    override val name: String = namePar
  }
}