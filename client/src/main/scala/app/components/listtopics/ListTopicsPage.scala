package app.components.listtopics

import app.Utils
import app.components._
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import shared.forms.{DataResponse, ErrorResponse}
import shared.pageparams.ListTopicsPageParams
import upickle.default._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.util.{Failure, Success}

object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected type State = ListTopicsState

  def apply(str: String) = comp(read[Props](str))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(_ => ListTopicsState())
    .renderBackend[Backend]
    .componentWillMount($ => $.modState(_.setGlobalScope(GlobalScope(
      pageParams = $.props,
      openOkDialog = str => $.backend.openOkDialog(str),
      openWaitPane = $.backend.openWaitPane,
      closeWaitPane = $.backend.closeWaitPane,
      checkParagraphAction = (par, newChecked) => $.backend.doAction(
        action = "check paragraph " + par.id + " to " + newChecked,
        doActionUrl = $.props.doActionUrl,
        onSuccess = _ => $.modState(_.checkParagraph(par.id.get, newChecked))
      ),
      expandParagraphAction = (par, newChecked) => $.backend.doAction(
        action = "expand paragraph " + par.id + " to " + newChecked,
        doActionUrl = $.props.doActionUrl,
        onSuccess = _ => $.modState(_.expandParagraph(par, newChecked))
      ),
      checkTopicAction = (topic, newChecked) => $.backend.doAction(
        action = "check topic " + topic.id + " to " + newChecked,
        doActionUrl = $.props.doActionUrl,
        onSuccess = _ => $.modState(_.checkTopic(topic.id.get, newChecked))
      ),
      paragraphCreated = p => $.modState(_.addParagraph(p)),
      paragraphUpdated = parUpd => $.modState(_.updateParagraph(parUpd)),
      paragraphDeleted = par => $.modState(_.deleteParagraph(par)) >> $.backend.closeWaitPane,
      topicCreated = topic => $.modState(_.addTopic(topic)),
      topicUpdated = topUpd => $.modState(_.updateTopic(topUpd)),
      topicDeleted = topId => $.modState(_.deleteTopic(topId))
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
          okDialogIfNecessary(state)
        )
    )

    def header(state: State, props: Props) = HeaderCmp(state.globalScope)

    def waitPaneIfNecessary(state: State): TagMod =
      if (state.waitPane) {
        if (state.infoToShow.isDefined) WaitPane() else WaitPane("rgba(255,255,255,0.0)")
      } else EmptyTag

    def okDialogIfNecessary(state: State): TagMod =
      if (state.infoToShow.isDefined) {
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(state.infoToShow.get),
            <.div(Button(id = "ok-diag-ok-btn", name = "OK", onClick = closeOkDialog))
          )
        )
      } else {
        EmptyTag
      }

    def openWaitPane: Callback = $.modState(_.copy(waitPane = true))
    def closeWaitPane: Callback = $.modState(_.copy(waitPane = false))
    def openOkDialog(text: String): Callback = openWaitPane >> $.modState(_.copy(infoToShow = Some(text)))
    def closeOkDialog: Callback = $.modState(_.copy(infoToShow = None)) >> closeWaitPane

    def doAction(action: String, doActionUrl: String, onSuccess: String => Callback): Callback =
      openWaitPane >> Utils.post(url = doActionUrl, data = action){
        case Success(DataResponse(str)) => onSuccess(str) >> closeWaitPane
        case Success(ErrorResponse(str)) =>
          println("ERROR - " + str)
          openOkDialog(str)
        case Failure(throwable) =>
          println("ERROR - " + throwable.getMessage)
          openOkDialog(throwable.getMessage)
        case _ => ???
      }.void
  }
}
