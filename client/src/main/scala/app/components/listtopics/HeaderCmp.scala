package app.components.listtopics

import app.Utils._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ScalaComponent}
import shared.dto.Paragraph

object HeaderCmp {

  protected case class Props(globalScope: ListTopicsPageGlobalScope/*, paragraphs: List[Paragraph]*/, filterChanged: String => Callback)

  protected case class State(newParagraphFormOpened: Boolean = false, filter: String = "")

  def apply(globalScope: ListTopicsPageGlobalScope/*, paragraphs: List[Paragraph]*/, filterChanged: String => Callback) =
    comp(Props(globalScope/*, paragraphs*/, filterChanged))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(^.`class`:=HeaderCmp.getClass.getSimpleName,
        <.div(
          buttonWithText(
            onClick = CallbackTo(???)/*p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, false)))*/,
            btnType = BTN_WARNING,
            text = "Collapse All"
          ),
          buttonWithText(
            onClick = $.modState(_.copy(newParagraphFormOpened = true)),
            btnType = BTN_WARNING,
            text = "Create paragraph"
          )/*,
          filter(p,s)*/
        ),
        if (s.newParagraphFormOpened)
          <.div(
            ParagraphForm.Props(
              paragraph = Paragraph(name = ""),
              submitFunction = paragraph => p.globalScope.wsClient.post(
                _.createParagraph(paragraph),
                th => p.globalScope.openOkDialog("Error creating paragraph: " + th.getMessage)
              ),
              cancelled = $.modState(_.copy(newParagraphFormOpened = false)),
              submitComplete = par =>
                $.modState(_.copy(newParagraphFormOpened = false)) >>
                  p.globalScope.paragraphCreated(par) >>
                  p.globalScope.closeWaitPane,
              textFieldLabel = "New paragraph:",
              submitButtonName = "Create",
              globalScope = p.globalScope
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