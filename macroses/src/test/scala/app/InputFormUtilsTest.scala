package app

import org.scalatest.{FlatSpec, Matchers}

class InputFormUtilsTest extends FlatSpec with Matchers {
  "InputFormUtils.field" should "produce correct setter" in {
    case class Person(name: String, age: Int)
    case class Field[T,F](name: String, get: T => F, set: (T,F) => T)
    trait Builder[T] extends InputFormUtils[T, List, Field] {
      override protected def fieldFromGetterAndSetter[F](name: String, get: (T) => F, set: (T, F) => T)(v: List[F]): Field[T, F] =
        Field(name, get, set)
    }
    val inst = new Builder[Person] {
      val field: Field[Person, Int] = field(_.age)(Nil)
    }

    inst.field.set(Person("rrrrr", 11), 78) should be(Person("rrrrr", 78))
  }

  "InputFormUtils.field" should "produce correct getter" in {
    case class Person(name: String, age: Int)
    case class Field[T,F](name: String, get: T => F, set: (T,F) => T)
    trait Builder[T] extends InputFormUtils[T, List, Field] {
      override protected def fieldFromGetterAndSetter[F](name: String, get: (T) => F, set: (T, F) => T)(v: List[F]): Field[T, F] =
        Field(name, get, set)
    }
    val inst = new Builder[Person] {
      val field: Field[Person, Int] = field(_.age)(Nil)
    }

    inst.field.get(Person("rrrrr", 11)) should be(11)
  }

  "InputFormUtils.field" should "set correct name" in {
    case class Person(name: String, age: Int)
    case class Field[T,F](name: String, get: T => F, set: (T,F) => T)
    trait Builder[T] extends InputFormUtils[T, List, Field] {
      override protected def fieldFromGetterAndSetter[F](name: String, get: (T) => F, set: (T, F) => T)(v: List[F]): Field[T, F] =
        Field(name, get, set)
    }
    val inst = new Builder[Person] {
      val field: Field[Person, Int] = field(_.age)(Nil)
    }

    inst.field.name should be("age")
  }

}
