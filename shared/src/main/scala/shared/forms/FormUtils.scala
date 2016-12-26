package shared.forms

import shared.forms.FormUtils.Message
import shared.messages.Language

trait InputTransformation[I,R] extends (I => Either[List[Message], R]) {self=>
  def >>[R2](next: InputTransformation[R,R2]): InputTransformation[I,R2] = new InputTransformation[I,R2] {
    override def apply(i: I): Either[List[Message], R2] = self(i).right.flatMap(next)
  }
}

object InputTransformation {
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

trait FormParts[T] {
  val formData: FormData
  val transformations: Map[String, InputTransformation[String, _]]
  def formData(obj: T): FormData
}

object FormUtils {
  type Message = Language => String

  implicit def tuple2ToTuple3[A, B, C](t: ((A, B), C)): (A, B, C) = (t._1._1, t._1._2, t._2)

  def form[T](elems: (String, InputTransformation[String, _], T => String)*): FormParts[T] = {
    implicit def forUnzip(elem: (InputElem, (String, InputTransformation[String, _], T => String))) = elem
    val (inputElems, transforms) = elems.toList.map {
      case (name, transformation, fromObj) => (
        InputElem(name = name), (name, transformation, fromObj)
      )
    }.unzip
    new FormParts[T] {
      val formData = FormData(inputElems)
      val transformations: Map[String, InputTransformation[String, _]] = transforms.map(t => (t._1, t._2)).toMap
      def formData(obj: T): FormData = transforms.foldLeft(formData)((fd, tpl) => fd.set(tpl._1, tpl._3(obj)))
    }
  }
}