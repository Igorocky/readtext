package app.components.listtopics

import app.components._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import org.scalajs.dom.ext.Ajax
import shared.forms.PostData
import shared.forms.PostDataTypes.DATA_RESPONSE
import shared.pageparams.ListTopicsPageParams
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected type State = ListTopicsState

  def apply(str: String) = comp(read[Props](str))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(_ => ListTopicsState())
    .renderBackend[Backend]
    .componentWillMount($ => $.modState(_.setGlobalScope(GlobalScope(
      pageParams = $.props,
      paragraphCreated = p => $.modState(_.addParagraph(p)),
      checkParagraphAction = (par, newChecked) => $.backend.doAction(
        action = "check paragraph " + par.id + " to " + newChecked,
        doActionUrl = $.props.doActionUrl,
        onSuccess = _ => $.modState(_.checkParagraph(par, newChecked))
      ),
      expandParagraphAction = (par, newChecked) => $.backend.doAction(
        action = "expand paragraph " + par.id + " to " + newChecked,
        doActionUrl = $.props.doActionUrl,
        onSuccess = _ => $.modState(_.expandParagraph(par, newChecked))
      ),
      checkTopicAction = (topic, newChecked) => $.backend.doAction(
        action = "check topic " + topic.id + " to " + newChecked,
        doActionUrl = $.props.doActionUrl,
        onSuccess = _ => $.modState(_.checkTopic(topic, newChecked))
      ),
      paragraphRenamed = par => $.modState(_.renameParagraph(par.id.get, par.name)),
      topicCreated = topic => $.modState(_.addTopic(topic)),
      topicUpdated = topic => $.modState(_.updateTopic(topic))
    ))))
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = UnivPage(
      language = state.globalScope.pageParams.headerParams.language,
      changeLangUrl = props.headerParams.changeLanguageUrl,
      onLanguageChange = newLang => $.modState(_.setLanguage(newLang)),
      content =
        <.div(
          header(state, props),
          state.paragraphs.map{paragraph =>
            ParagraphCmp(paragraph, state.globalScope)
          },
          waitPaneIfNecessary(state),
          errorDialogIfNecessary(state)
        )
    )

    def header(state: State, props: Props) = HeaderCmp(state.globalScope)

    def waitPaneIfNecessary(state: State): TagMod =
      if (state.waitPane) {
        if (state.errorDesc.isDefined) WaitPane() else WaitPane("rgba(255,255,255,0.0)")
      } else EmptyTag

    def errorDialogIfNecessary(state: State): TagMod =
      if (state.errorDesc.isDefined) {
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div("Error: " + state.errorDesc.get),
            <.div(Button(id = "err-diag-ok-btn", name = "OK", onClick = closeErrorDialog))
          )
        )
      } else {
        EmptyTag
      }

    def openWaitPane: Callback = $.modState(_.copy(waitPane = true))
    def closeWaitPane: Callback = $.modState(_.copy(waitPane = false))
    def openErrorDialog(errDesc: String): Callback = openWaitPane >> $.modState(_.copy(errorDesc = Some(errDesc)))
    def closeErrorDialog: Callback = $.modState(_.copy(errorDesc = None)) >> closeWaitPane

    def doAction(action: String, doActionUrl: String, onSuccess: String => Callback): Callback =
      openWaitPane >> Callback.future {
        Ajax.post(url = doActionUrl, data = action).map(r => read[PostData](r.responseText)).map { resp =>
          if (resp.typ == DATA_RESPONSE) {
            println("OK - " + resp.content)
            onSuccess(resp.content) >> closeWaitPane
          } else {
            println("ERROR - " + resp.content)
            openErrorDialog(resp.content)
          }
        }.recover{
          case throwable =>
            println("ERROR - " + throwable.getMessage)
            openErrorDialog(throwable.getMessage)
        }
      }
  }
}
