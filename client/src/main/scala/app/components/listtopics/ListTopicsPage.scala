package app.components.listtopics

import app.components._
import app.components.listtopics.ParagraphCmp.NewValueChecked
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import org.scalajs.dom.ext.Ajax
import shared.dto.{Paragraph, Topic}
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
    .initialState_P(p => ListTopicsState(p.headerParams.language, p.doActionUrl, p.paragraphs))
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = UnivPage(
      language = state.lang,
      changeLangUrl = props.headerParams.changeLanguageUrl,
      onLanguageChange = newLang => $.modState(_.copy(lang = newLang)),
      content =
        <.div(
          header(state, props),
          state.paragraphs.map{paragraph =>
            ParagraphCmp(
              paragraph = paragraph,
              checkParagraphAction = checkParagraphAction(paragraph, props),
              expandParagraphAction = expandParagraphAction(paragraph, props),
              checkTopicAction = checkTopicAction(paragraph, props)
            )
          },
          waitPaneIfNecessary(state),
          errorDialogIfNecessary(state)
        )
    )

    def header(state: State, props: Props) =
      HeaderCmp(
        language = props.headerParams.language,
        createParagraphUrl = props.createParagraphUrl,
        paragraphCreated = p => $.modState(_.addParagraph(p))
      )

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

    def checkParagraphAction(paragraph: Paragraph, props: Props) = (newChecked: Boolean) => doAction(
        action = "check paragraph " + paragraph.id + " to " + newChecked,
        doActionUrl = props.doActionUrl,
        onSuccess = _ => $.modState(_.checkParagraph(paragraph, newChecked))
      )

    def checkTopicAction(paragraph: Paragraph, props: Props): (Topic, NewValueChecked) => Callback =
      (topic: Topic, newChecked: Boolean) => doAction(
        action = "check topic " + topic.id + " to " + newChecked,
        doActionUrl = props.doActionUrl,
        onSuccess = _ => $.modState(_.checkTopic(paragraph, topic, newChecked))
      )

    def expandParagraphAction(paragraph: Paragraph, props: Props) = (newChecked: Boolean) => doAction(
      action = "expand paragraph " + paragraph.id + " to " + newChecked,
      doActionUrl = props.doActionUrl,
      onSuccess = _ => $.modState(_.expandParagraph(paragraph, newChecked))
    )

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
        }.recover {
          case throwable =>
            println("ERROR - " + throwable.getMessage)
            openErrorDialog(throwable.getMessage)
        }
      }
  }
}
