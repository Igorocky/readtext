package css

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
    bottom(0 px)
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
    position.fixed,
    backgroundColor(rgba(80,80,80,0.5))
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

  "." + PARAGRAPH_NAME - (
    fontSize(30 px),
    fontWeight.bold
  )

  case class Color(r: Int, g: Int, b: Int) {
    def more(pct: Double) = {
      val factor = 1 + pct
      copy(r = (r*factor).toInt,g = (g*factor).toInt,b = (b*factor).toInt)
    }
    def less(pct: Double) = {
      val factor = 1 - pct
      copy(r = (r*factor).toInt,g = (g*factor).toInt,b = (b*factor).toInt)
    }
    def forCss = rgb(r,g,b)
  }
//  val color = Color(76,201,63)
//  ".HeaderCmp button.new-par" - (
//    backgroundColor(color.forCss),
//    border(none),
//    borderRadius(5 px),
//    &.hover - (
//      backgroundColor(color.more(0.25).forCss)
//      )
//    )

  ".HeaderCmp" - (
    margin(10 px)
  )

  ".HeaderCmp input" - (
    display.inlineBlock,
    margin(0 px, 2 px)
  )

  ".ParagraphCmp" - (
    padding(5 px),
    borderRadius(10 px)
  )

  ".ParagraphCmp.checked" - (
    backgroundColor(rgb(200,200,200))
  )

  s".$SELECTED_PARAGRAPH" - (
    backgroundColor(rgb(102, 255, 255)),
    borderRadius(5 px)
  )

  ".TopicCmp" - (
    margin(0 px, 5 px, 5 px, 0 px),
    padding(5 px),
    borderRadius(10 px)
  )

  ".TopicCmp.checked" - (
    backgroundColor(rgb(140,140,140))
  )

  ".TopicCmp img" - (
    marginLeft(10 px)
  )

  "div." + HIGHLIGHT_CHILD_SPAN_ON_HOVER + ":hover > span." + HIGHLIGHTED - (
    backgroundColor(yellow),
    borderRadius(5 px)
  )

//  "div." + HIGHLIGHT_CHILD_SPAN_ON_HOVER + ":hover" - (
//    outline(solid, 2 px, yellow)
//  )

  ".btn" - (
    marginLeft(2 px),
    marginRight(2 px)
  )

  ".form" - (
    backgroundColor(c"#d9e9f7"),
    marginTop(10 px),
    borderRadius(10 px),
    padding(10 px),
    fontSize(14 px),
    fontWeight.normal
  )

  ".form button" - (
    marginTop(10 px)
  )

  ".modal-diag-content" - (
    borderRadius(10 px)
  )

  ".ScoreCmp" - (
    marginTop(10 px)
  )

  ".TagsCmp > div" - (
    display.inlineBlock
  )

  ".TagsCmp > div.has-error > div" - (
    display.none
  )

  s".$TAG" - (
    backgroundColor(c"#2af32a"),
    borderRadius(10 px),
    padding(1 px, 10 px, 1 px, 10 px),
    margin(2 px)
  )

  s".$REM_TAG_BTN" - (
    backgroundColor.grey,
    borderRadius(10 px),
    color.white,
    padding(0 px, 5 px),
    marginLeft(3 px),
    marginRight(-10 px),
    cursor.pointer,
    &.hover - (
      backgroundColor.lightgrey
    )
  )

  s".$TREE_TD_EXPAND_BUTTON" - (
    verticalAlign.top
  )

  s".$MAIN_TOPIC_TREE .$TREE_TD_EXPAND_BUTTON" - (
    paddingTop(18 px)
  )

  s".$TREE_NODE_VALUE" - (
    display.inlineBlock
  )

  s".$TOPIC_HISTORY_TABLE, .$TOPIC_HISTORY_TABLE td, .$TOPIC_HISTORY_TABLE th" - (
    border(1 px, solid, black),
    borderCollapse.collapse
  )

  s".$TOPIC_HISTORY_TABLE th, .$TOPIC_HISTORY_TABLE td" - (
    padding(1 px, 10 px)
    )

  s".$TOPIC_STATUSES_TABLE, .$TOPIC_STATUSES_TABLE td, .$TOPIC_STATUSES_TABLE th" - (
    border(1 px, solid, black),
    borderCollapse.collapse
  )

  s".$EASINESS_SCORE" - (
    color(white)
  )

  s".$TOPIC_STATUSES_TABLE .$EASINESS_EASY" - (
    backgroundColor(rgb(92, 184, 92))/*green*/
  )

  s".$TOPIC_STATUSES_TABLE .$EASINESS_MEDIUM" - (
    backgroundColor(rgb(240, 173, 78))/*orange*/
  )

  s".$TOPIC_STATUSES_TABLE .$EASINESS_HARD" - (
    backgroundColor(rgb(217, 83, 79))/*red*/
  )

  s".$TOPIC_STATUSES_TABLE .$SCORE_EXCELLENT" - (
    backgroundColor(rgb(92, 184, 92))/*green*/
  )

  s".$TOPIC_STATUSES_TABLE .$SCORE_GOOD" - (
    backgroundColor(rgb(91, 192, 222))/*blue*/
  )

  s".$TOPIC_STATUSES_TABLE .$SCORE_POOR" - (
    backgroundColor(rgb(240, 173, 78))/*orange*/
  )

  s".$TOPIC_STATUSES_TABLE .$SCORE_BAD" - (
    backgroundColor(rgb(217, 83, 79))/*red*/
  )

  s".$TOPIC_STATUSES_TABLE tr:hover" - (
    backgroundColor(yellow)
  )

  s".$TOPIC_STATUSES_TABLE th, .$TOPIC_STATUSES_TABLE td" - (
    padding(1 px, 10 px)
  )

  s".$TOPIC_ACTION_BUTTONS" - (
    marginTop(5 px)
  )

  s".$TOPIC_CURR_STATE" - (
    marginTop(3 px)
  )


}
