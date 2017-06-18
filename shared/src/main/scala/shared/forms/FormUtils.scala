package shared.forms

import app.InputFormUtils
import shared.forms.InputTransformation.Message
import shared.messages.Language

trait InputTransformation[I,R] extends (I => Either[List[Message], R]) {self=>
  def >>[R2](next: InputTransformation[R,R2]): InputTransformation[I,R2] = new InputTransformation[I,R2] {
    override def apply(i: I): Either[List[Message], R2] = self(i).right.flatMap(next)
  }
}

object InputTransformation {
  type Message = Language => String

  def apply[I,R](f: I => R): InputTransformation[I,R] = new InputTransformation[I,R] {
    def apply(i: I) = Right(f(i))
  }
}

trait InputValidation[I] extends InputTransformation[I,I] {self=>
  def >>(next: InputValidation[I]): InputValidation[I] = new InputValidation[I] {
    override def apply(i: I) = self.apply(i) match {
      case r: Right[_, I] => next(i)
      case l1 @ Left(errors1) => next(i) match {
        case r: Right[_, I] => l1
        case l2 @ Left(errors2) => Left(errors1:::errors2)
      }
    }
  }
}

object InputValidation {
  def apply[I](f: I => Boolean, msg: Message): InputValidation[I] = new InputValidation[I] {
    override def apply(i: I): Either[List[Message], I] = if (!f(i)) {
      Left(List(msg))
    } else {
      Right(i)
    }
  }
}

case class FormField[T,F](name: String, getter: FormMethods[T]#Extractor[F], setter: FormMethods[T]#Setter[F], validation: InputValidation[F]) {
  def validate(formData: FormData[T]): FormData[T] = {
    val errorMsgs = validate(formData.data, formData.language)
    if (errorMsgs.isEmpty) {
      formData.copy(errors = formData.errors - name)
    } else {
      formData.copy(errors = formData.errors + (name -> errorMsgs))
    }
  }

  def validate(obj: T, lang: Language): List[String] = validation(getter(obj)).left.getOrElse(Nil).map(_(lang))

  def set(value: F, formData: FormData[T]): FormData[T] = formData.copy(data = setter(formData.data, value))

  def setAndValidate(value: F, formData: FormData[T]): FormData[T] = validate(set(value, formData))

  def get(formData: FormData[T]): F = getter(formData.data)

  def errors(formData: FormData[T]): List[String] = formData.errors.get(name).getOrElse(Nil)
}

trait FormMethods[T] extends shared.utils.Enum[FormField[T, _]] with InputFormUtils[T, InputValidation, FormField] {
  type Message = Language => String
  type Extractor[F] = T => F
  type Setter[F] = (T, F) => T

  def validate(formData: FormData[T]): FormData[T] =
    allElems.foldLeft(formData){case (fd, field) => field.validate(fd)}

  def validate(lang: Language, obj: T): FormData[T] =
    validate(FormData(lang, obj))

  def changeLang(newLanguage: Language, formData: FormData[T]): FormData[T] =
    if (newLanguage == formData.language) formData
    else validate(formData.copy(language = newLanguage))

  override protected def fieldFromGetterAndSetter[F](name: String, get: Extractor[F], set: Setter[F])
                                                    (validation: InputValidation[F]): FormField[T, F] =
    addElem(FormField(name, get, set, validation))
}
