package app

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

class WsMacroImpl(val c: Context) {
  import c.universe._

  def post[A: c.WeakTypeTag, O](method: Expr[_], errHnd: Expr[_]) = {
    val q"$client.$_[$outputType](_.$methodName(..$args), $errorHandler)" = c.macroApplication
    val path: String = c.weakTypeOf[A] + "." + methodName
    q"""
        $client.doCall(
                  $path,
                  _root_.upickle.default.write((..$args)),
                  _root_.upickle.default.read[$outputType](_),
                  $errorHandler
        )
      """
  }

  def forMethod[A: c.WeakTypeTag, I, O](method: Expr[_]) = {
    val q"$_[..$argTypes, $outputType](($_) => { (..$_) => $_.$methodName(..$_) })" = c.macroApplication
    val path: String = c.weakTypeOf[A] + "." + methodName
    q"""
       ($path, _root_.upickle.default.read[(..$argTypes)], _root_.upickle.default.write(_:$outputType))
     """
  }
}

trait WsClientMacro[A, C] {
  def post[O](method: A => O, errHnd: Throwable => C): (O => C) => C = macro WsMacroImpl.post[A, O]

  def doCall[O](path: String,
                          dataStr: String,
                          reader: String => O,
                          errHnd: Throwable => C): (O => C) => C
}

trait RouterBuilderUtils[A] {
  protected def forMethod[I, O](method: A => I => O): (String, String => I, O => String) = macro WsMacroImpl.forMethod[A, I, O]

  protected def forMethod2[I1, I2, O](method: A => (I1, I2) => O): (String, String => (I1, I2), O => String) = macro WsMacroImpl.forMethod[A, (I1, I2), O]

  protected def forMethod3[I1, I2, I3, O](method: A => (I1, I2, I3) => O): (String, String => (I1, I2, I3), O => String) = macro WsMacroImpl.forMethod[A, (I1, I2, I3), O]
}