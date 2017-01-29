package app.components

import app.components.forms.NewParagraph
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB}
import org.scalajs.dom.ext.Ajax
import shared.dto.{Paragraph, Topic}
import shared.forms.PostData
import shared.forms.PostDataTypes.DATA_RESPONSE
import shared.messages.Language
import shared.pageparams.ListTopicsPageParams
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ListTopicsPage {
  protected type Props = ListTopicsPageParams

  protected case class State(
                              lang: Language,
                              doActionUrl: String,
                              paragraphs: List[Paragraph],
                              newParagraphOpened: Boolean = false,
                              waitPane: Boolean = false,
                              errorDesc: Option[String] = None
                            )

  def apply(str: String) = comp(read[Props](str))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(p.headerParams.language, p.doActionUrl, p.paragraphs))
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
          state.paragraphs.map{p =>
            <.div(
              <.div(
                checkboxForParagraph(p, props),
                expandParagraphButton(p, props),
                p.name + " - " + p.checked
              ),
              if (p.expanded) listTopics(p, props) else EmptyTag
            )
          },
          waitPaneIfNecessary(state),
          errorDialogIfNecessary(state)
        )
    )

    def header(state: State, props: Props) =
      <.div(
        <.div(
          Button(
            id = "open-new-par-diag-btn",
            name = "Create paragraph",
            onClick = $.modState(_.copy(newParagraphOpened = true))
          )
        ),
        <.div(
          if (state.newParagraphOpened)
            NewParagraph(
              onOk = newPar => doAction(
                action = "create new paragraph with name '" + newPar + "'",
                doActionUrl = props.doActionUrl,
                onSuccess = newParId => $.modState(_.copy(paragraphs = state.paragraphs::: Paragraph(
                  id = Some(newParId.toLong),
                    checked = false,
                    name = newPar,
                    expanded = false,
                    order = state.paragraphs.lastOption.map(_.order + 1).getOrElse(0),
                    topics = Nil
                  ) :: Nil, newParagraphOpened = false))
              ),
              onCancel = $.modState(_.copy(newParagraphOpened = false))
            )
          else
            EmptyTag
        )
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

    def checkboxForParagraph(p: Paragraph, props: Props) =
      Checkbox(
        id = "selectPar-" + p.id.get,
        onChange = newChecked => doAction(
          action = "check paragraph " + p.id + " to " + newChecked,
          doActionUrl = props.doActionUrl,
          onSuccess = _ => checkParagraph(p, newChecked)
        ),
        checked = p.checked
      )

    def checkboxForTopic(p: Paragraph, t: Topic, props: Props) =
      Checkbox(
        id = "selectTopic-" + t.id.get,
        onChange = newChecked => doAction(
          action = "check topic " + t.id + " to " + newChecked,
          doActionUrl = props.doActionUrl,
          onSuccess = _ => checkTopic(p, t, newChecked)
        ),
        checked = t.checked
      )

    def expandParagraphButton(p: Paragraph, props: Props) =
      Button(
        id = "expandPar-" + p.id.get,
        name = if (p.expanded) "-" else "+",
        onClick = doAction(
          action = "expand paragraph " + p.id + " to " + !p.expanded,
          doActionUrl = props.doActionUrl,
          onSuccess = _ => expandParagraph(p, !p.expanded)
        )
      )

    def listTopics(p: Paragraph, props: Props) =
      p.topics.map{t=>
        <.div(
          checkboxForTopic(p, t, props),
          t.title + " - " + t.checked
        )
      }

    def checkParagraph(p: Paragraph, newChecked: Boolean): Callback =
      $.modState(s => modParagraphById(s, p.id.get, _.copy(checked = newChecked)))

    def checkTopic(p: Paragraph, t: Topic, newChecked: Boolean): Callback =
      $.modState(s => modTopicById(s, p.id.get, t.id.get, _.copy(checked = newChecked)))

    def expandParagraph(p: Paragraph, newExpanded: Boolean): Callback =
      $.modState(s => modParagraphById(s, p.id.get, _.copy(expanded = newExpanded)))

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


    def modParagraphById(ps: List[Paragraph], parId: Long, mod: Paragraph => Paragraph): List[Paragraph] =
      ps.map(p => if (p.id.get == parId) mod(p) else p)

    def modParagraphById(state: State, parId: Long, mod: Paragraph => Paragraph): State =
      state.copy(paragraphs = modParagraphById(state.paragraphs, parId, mod))

    def modTopicById(ts: List[Topic], topId: Long, mod: Topic => Topic): List[Topic] =
      ts.map(t => if (t.id.get == topId) mod(t) else t)

    def modTopicById(state: State, parId: Long, topId: Long, mod: Topic => Topic): State =
      state.copy(
        paragraphs = modParagraphById(
          state.paragraphs, parId, p => p.copy(topics = modTopicById(p.topics, topId, mod))
        )
      )

  }
}
