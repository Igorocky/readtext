package shared.forms

import shared.messages.Language

case class FormData(language: Language, inputElems: List[InputElem], submitUrl: String = "") {
  def get(name: String): InputElem = inputElems.find(_.name == name).get

  def set(name: String, value: String, errors: List[String] = Nil): FormData = {
    val old = get(name)
    copy(inputElems = old.copy(value = value, errors = errors)::inputElems.filter(_ ne old))
  }

  def validate(transformations: Map[String, InputTransformation[String, _]]): FormData = copy(
    inputElems = inputElems.map(ie =>
      ie.copy(
        errors = validate(ie.name, ie.value, transformations)
      )
    )
  )

  def createSetter(name: String,
                   transformations: Map[String, InputTransformation[String, _]])
                  (newValue: String): FormData =
    set(
      name,
      newValue,
      validate(name, newValue, transformations)
    )

  lazy val hasErrors = inputElems.exists(_.errors.nonEmpty)

  def values(transformations: Map[String, InputTransformation[String, _]]): Either[FormData, Map[String, _]] = {
    val values = inputElems.map(ie => (ie.name, ie.value, transformations(ie.name)(ie.value)))
    if (values.exists(_._3.isLeft)) {
      Left(copy(inputElems = values.map(v =>
        InputElem(
          name = v._1,
          value = v._2,
          errors = v._3.left.getOrElse(Nil).map(_(language)))
      )))
    } else {
      Right(values.map(v => (v._1, v._3.right.get)).toMap)
    }
  }

  private def validate(name: String, value: String,
                       transformations: Map[String, InputTransformation[String, _]]): List[String] =
    transformations(name)(value).left.getOrElse(Nil).map(_(language))
}
