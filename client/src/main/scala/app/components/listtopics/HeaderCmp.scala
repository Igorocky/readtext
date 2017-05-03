package app.components.listtopics

import app.Utils._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactEventFromInput, ReactKeyboardEvent, ScalaComponent}
import shared.dto.Paragraph
import shared.forms.Forms
import upickle.default.read

object HeaderCmp {

  protected case class Props(globalScope: GlobalScope, paragraphs: List[Paragraph], filterChanged: String => Callback)

  protected case class State(newParagraphFormOpened: Boolean = false, filter: String = "")

  def apply(globalScope: GlobalScope, paragraphs: List[Paragraph], filterChanged: String => Callback) =
    comp(Props(globalScope, paragraphs, filterChanged))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(^.`class`:=HeaderCmp.getClass.getSimpleName,
        <.div(
          buttonWithText(
            onClick = p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, true))),
            btnType = BTN_WARNING,
            text = "Expand All"
          ),
          buttonWithText(
            onClick = p.globalScope.expandParagraphsAction(p.paragraphs.map(p => (p.id.get, false))),
            btnType = BTN_WARNING,
            text = "Collapse All"
          ),
          buttonWithText(
            onClick = $.modState(_.copy(newParagraphFormOpened = true)),
            btnType = BTN_WARNING,
            text = "Create paragraph"
          ),
          filter(p,s)
        ),
        if (s.newParagraphFormOpened)
          <.div(
            ParagraphForm(
              formData = Forms.paragraphForm.formData(
                language = p.globalScope.pageParams.headerParams.language,
                submitUrl = p.globalScope.pageParams.createParagraphUrl
              ),
              cancelled = $.modState(_.copy(newParagraphFormOpened = false)),
              submitComplete = str =>
                $.modState(_.copy(newParagraphFormOpened = false)) >>
                  p.globalScope.paragraphCreated(read[Paragraph](str)),
              textFieldLabel = "New paragraph:",
              submitButtonName = "Create",
              globalScope = p.globalScope
            )
          )
        else
          EmptyVdom
      )
    }

    def filter(props: Props, state: State) = TagMod(
      <.datalist(
        ^.id:="tag-filter",
        props.paragraphs.flatMap(_.topics).flatMap(_.tags).distinct.toVdomArray{tagStr=>
          <.option(
            ^.key:=tagStr,
            ^.value:=tagStr
          )
        }
      ),
      <.input.text(
        ^.`class`:="form-control",
        ^.list:="tag-filter",
        ^.placeholder:="tag filter",
        ^.value := state.filter,
        ^.onChange ==> { (e: ReactEventFromInput) =>
          val newValue = e.target.value
          $.modState(_.copy(filter = newValue))
        },
        ^.onKeyDown ==> { (e: ReactKeyboardEvent) =>
          if (e.keyCode == 13) {
            props.filterChanged(state.filter)
          } else {
            Callback.empty
          }
        },
        ^.maxWidth:="200px"
      )
    )
  }
}