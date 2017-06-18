package app

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

class InputFormMacroImpl(val c: Context) {
  import c.universe._

  def field[T: c.WeakTypeTag,F](get: c.Expr[T => F]) = {
    val q"$_[$fieldType](_.$fieldName)" = c.macroApplication
    val fieldNameStr = fieldName.toString()
    q"fieldFromGetterAndSetter($fieldNameStr, _.$fieldName, (o: ${c.weakTypeOf[T]}, v: $fieldType) => o.copy($fieldName = v))"
  }

}

trait InputFormUtils[T,V[_],FF[T,_]] {
  protected def fieldFromGetterAndSetter[F](name: String, get: T => F, set: (T,F) => T)(v:V[F]):FF[T,F]
  protected def field[F](get: T => F): V[F] => FF[T,F] = macro InputFormMacroImpl.field[T,F]
}