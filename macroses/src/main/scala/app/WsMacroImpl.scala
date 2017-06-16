package app

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

class WsMacroImpl(val c: Context) {
  import c.universe._

  def postImpl[A: c.WeakTypeTag, O: c.WeakTypeTag](method: Expr[_], errHnd: Expr[_]) = {
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

}

trait Api {
  def updateTopic(id: Long, data: String): List[String]
}

trait WsClient[A,C] {
  def post[O](method: A => O, errHnd: Throwable => C): (O => C) => C = macro WsMacroImpl.postImpl[A,O]

  def doCall[O](path: String,
                dataStr: String,
                reader: String => O,
                errHnd: Throwable => C): (O => C) => C
}