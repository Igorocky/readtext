package app.components.listtopics

import app.Utils._
import app.components.WindowFunc
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ScalaComponent}
import shared.dto.Paragraph

object HeaderCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext,
                   /* paragraphs: List[Paragraph],*/
                   filterChanged: String => Callback) {
    @inline def render = comp(this)
  }

  protected case class State(newParagraphFormOpened: Boolean = false, filter: String = "")

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(implicit p: Props, s: State) = {
      <.div(^.`class`:=HeaderCmp.getClass.getSimpleName,
        <.div(
          buttonWithText(
            onClick = p.ctx.collapseAllAction,
            btnType = BTN_WARNING,
            text = "Collapse All"
          ),
          buttonWithText(
            onClick = $.modState(_.copy(newParagraphFormOpened = true)),
            btnType = BTN_WARNING,
            text = "Create paragraph"
          ),
          if (!p.ctx.listTopicsPageMem.selectMode) {
            buttonWithText(
              onClick = p.ctx.gotoSelectMode,
              btnType = BTN_INFO,
              text = "Move mode"
            )
          } else {
            TagMod(
              // TODO: select target should be disabled when nothing is selected
              buttonWithText(
                onClick = p.ctx.openSelectParagraphWindow,
                btnType = BTN_INFO,
                text = "Select target"
              ),
              buttonWithText(
                onClick = p.ctx.cancelSelectMode,
                btnType = BTN_INFO,
                text = "Cancel move"
              )
            )
          }
          /*,
          filter(p,s)*/
        ),
        if (s.newParagraphFormOpened)
          <.div(
            ParagraphForm.Props(
              ctx = p.ctx,
              paragraph = Paragraph(name = ""),
              submitFunction = paragraph => p.ctx.wsClient.post(
                _.createParagraph(paragraph),
                th => p.ctx.openOkDialog("Error creating paragraph: " + th.getMessage)
              ),
              cancelled = $.modState(_.copy(newParagraphFormOpened = false)),
              submitComplete = par =>
                $.modState(_.copy(newParagraphFormOpened = false)) >>
                  p.ctx.paragraphCreated(par) >>
                  p.ctx.closeWaitPane,
              textFieldLabel = "New paragraph:",
              submitButtonName = "Create"
            ).render
          )
        else
          EmptyVdom
      )
    }

//    def filter(props: Props, state: State) = TagMod(
//      <.datalist(
//        ^.id:="tag-filter",
//        props.paragraphs.flatMap(_.topics).flatMap(_.tags).distinct.toVdomArray{tagStr=>
//          <.option(
//            ^.key:=tagStr,
//            ^.value:=tagStr
//          )
//        }
//      ),
//      <.input.text(
//        ^.`class`:="form-control",
//        ^.list:="tag-filter",
//        ^.placeholder:="tag filter",
//        ^.value := state.filter,
//        ^.onChange ==> { (e: ReactEventFromInput) =>
//          val newValue = e.target.value
//          $.modState(_.copy(filter = newValue))
//        },
//        ^.onKeyDown ==> { (e: ReactKeyboardEvent) =>
//          if (e.keyCode == 13) {
//            props.filterChanged(state.filter)
//          } else {
//            Callback.empty
//          }
//        },
//        ^.maxWidth:="200px"
//      )
//    )
  }
}