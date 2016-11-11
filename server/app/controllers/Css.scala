package controllers

import scalacss.Defaults._
import scalacss.DslBase.ToStyle

object Css extends StyleSheet.Standalone {
  import dsl._

  private def seq(list: List[ToStyle], t: ToStyle*): Seq[ToStyle] = list ::: (t.toList) ::: Nil

  private val shieldProps = List[ToStyle](
    left(0 px),
    right(0 px),
    top(0 px),
    bottom(0 px),
    backgroundColor(rgba(80,80,80,0.5))
  )

  private val waitPaneProps = shieldProps:::List[ToStyle](
    cursor.wait_
  )

  ".sdfrew" - (seq(waitPaneProps,
    position.absolute
  ): _*)
}
