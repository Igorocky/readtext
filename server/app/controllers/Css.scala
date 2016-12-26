package controllers

import shared.SharedConstants._

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

  "." + WAIT_PANE_ABSOLUTE - (seq(waitPaneProps,
    position.absolute
  ): _*)

  "." + WAIT_PANE_FIXED - (seq(waitPaneProps,
    position.fixed
  ): _*)

  "." + MODAL_DIAG_BACK_PANE - (seq(shieldProps,
    position.fixed
  ): _*)

  "." + MODAL_DIAG_CONTENT - (
    position.relative,
    margin(50 px, auto),
    backgroundColor(white),
    resize.both,
    overflow.auto,
    padding(15 px)
  )

  "." + EDITABLE_DIV - (
    border(solid, 1 px, white),
    borderRadius(2 px),
    &.hover - (
      border(solid, 1 px, darkgray)
      )

  )

  "." + EDITABLE_DIV_CHANGED - (
    backgroundColor(yellow)
    )

  "." + EDITABLE_DIV_EMPTY - (
    opacity(0)
    )
}
