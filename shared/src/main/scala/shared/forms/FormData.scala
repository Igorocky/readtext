package shared.forms

import shared.messages.Language

case class FormData(language: Language, inputElems: List[InputElem], submitUrl: String = "") {
  def get(key: FormKey): InputElem = inputElems.find(_.name == key.name).get

  def set(key: FormKey, value: String, errors: List[String] = Nil): FormData = {
    val old = get(key)
    copy(inputElems = old.copy(value = value, errors = errors)::inputElems.filter(_ ne old))
  }

  def validate(transformations: Map[String, InputTransformation[String, _]]): FormData = copy(
    inputElems = inputElems.map(ie =>
      ie.copy(
        errors = validate(ie.name, ie.value, transformations)
      )
    )
  )

  def createSetter(key: FormKey,
                   transformations: Map[String, InputTransformation[String, _]])
                  (newValue: String): FormData =
    set(
      key,
      newValue,
      validate(key.name, newValue, transformations)
    )

  def set(key: FormKey, newValue: String,
          transformations: Map[String, InputTransformation[String, _]]): FormData =
    set(
      key,
      newValue,
      validate(key.name, newValue, transformations)
    )

  lazy val hasErrors = inputElems.exists(_.errors.nonEmpty)

  def values(transformations: Map[String, InputTransformation[String, _]]): Either[FormData, FormValues] = {
    val values = inputElems.map(ie => (ie.name, ie.value, transformations(ie.name)(ie.value)))
    if (values.exists(_._3.isLeft)) {
      Left(copy(inputElems = values.map(v =>
        InputElem(
          name = v._1,
          value = v._2,
          errors = v._3.left.getOrElse(Nil).map(_(language)))
      )))
    } else {
      Right(new FormValues {
        private val vals = values.map(v => (v._1, v._3.right.get)).toMap
        override def apply(key: FormKey): key.ValueType = vals(key.name).asInstanceOf[key.ValueType]
      })
    }
  }

  private def validate(name: String, value: String,
                       transformations: Map[String, InputTransformation[String, _]]): List[String] =
    transformations(name)(value).left.getOrElse(Nil).map(_(language))
}

trait FormKey {
  type ValueType
  val name: String
}

trait FormValues {
  def apply(key: FormKey): key.ValueType
}
