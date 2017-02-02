package app.components

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactElement}
import org.scalajs.dom.ext.Ajax
import shared.SharedConstants._
import shared.forms.Forms
import shared.messages.Language
import shared.pageparams.{ListTextsPageParams, TextUI}
import upickle.default._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ListTextsPage {
  protected type Props = ListTextsPageParams

  protected case class State(
                              lang: Language,
                              texts: List[TextUI],
                              addTextDialogOpened: Boolean = false,
                              deleteText: Option[TextUI] = None,
                              editText: Option[TextUI] = None,
                              waitPane: Boolean = false
                            )

  def apply(str: String) = comp(read[Props](str))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(p.headerParams.language, p.texts))
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = UnivPage(
      language = state.lang,
      changeLangUrl = props.headerParams.changeLanguageUrl,
      onLanguageChange = newLang => $.modState(_.copy(lang = newLang)),
      content =
        <.div(
          <.div(
            Button(id = "new-text-btn", name = "New text", onClick = $.modState(_.copy(addTextDialogOpened = true)))
          ),
          tableWithTexts(props, state),
          addTextDialog(props, state),
          editTextDialog(props, state),
          deleteTextDialog(props, state),
          if (state.waitPane) WaitPane() else EmptyTag
        )
    )

    def loadFullText(textUI: TextUI, loadFullTextUrl: String): Callback = Callback.future(Ajax.post(
      url = loadFullTextUrl,
      data = textUI.id.get.toString
    ).map(resp => $.modState(_.copy(editText = Some(read[TextUI](resp.responseText))))))

    def tableWithTexts(props: Props, state: State): ReactElement = {
      implicit val lang = state.lang
      <.div(
        state.texts.map{text=>
          <.div(
            <.hr(^.width:="100%"),
            <.h2(text.title),
            <.div(text.content),
            <.div(
              Button(
                id = "edit-btn-" + text.id.get,
                name = "Edit",
                onClick = loadFullText(text, props.loadFullTextUrl)
              ),
              Button(
                id = "delete-btn-" + text.id.get,
                name = "Delete",
                onClick = $.modState(_.copy(deleteText = Some(text)))
              )
            ),
            <.hr(^.width:="100%")
          )
        }
      )
    }

    def addTextDialog(props: Props, state: State): TagMod = {
      if (state.addTextDialogOpened) {
        ModalDialog(
          width = "400px",
          content = TextForm(
            language = state.lang,
            formData = Forms.textForm.formData(submitUrl = props.mergeTextUrl, language = state.lang),
            submitButtonName = "Create",
            cancelled = $.modState(_.copy(addTextDialogOpened = false)),
            submitComplete = text => $.modState(_.copy(texts = text :: state.texts, addTextDialogOpened = false))
          )
        )
      } else {
        EmptyTag
      }
    }

    def editTextDialog(props: Props, state: State): TagMod = {
      if (state.editText.isDefined) {
        val textToEdit = state.editText.get
        ModalDialog(
          width = "400px",
          content = TextForm(
            language = state.lang,
            formData = Forms.textForm.formData(
              obj = textToEdit,
              submitUrl = props.mergeTextUrl,
              language = state.lang
            ),
            submitButtonName = "Update",
            cancelled = $.modState(_.copy(editText = None)),
            submitComplete = updatedText => $.modState(_.copy(
              texts = replaceById(state.texts, updatedText.copy(content = updatedText.content.substring(0, 200 min updatedText.content.length) + "...")),
              editText = None
            )),
            editMode = true
          )
        )
      } else {
        EmptyTag
      }
    }

    def replaceById(list: List[TextUI], newText: TextUI) =
      list.foldLeft(List[TextUI]()) { (list, tx) =>
        (if (tx.id == newText.id) newText else tx) :: list
      }.reverse

    def deleteTextDialog(props: Props, state: State): TagMod = {
      if (state.deleteText.isDefined) {
        ModalDialog(
          width = "500px",
          content =
            <.div(
              <.div(s"Are you sure you want to delete the text '${state.deleteText.get.title}'?"),
              <.div(
                Button(
                  id = "yes-btn",
                  name = "Yes",
                  onClick = $.modState(_.copy(waitPane = true)) >> deleteText(props, state)
                ),
                Button(id = "cancel-btn", name = "Cancel", onClick = $.modState(_.copy(deleteText = None)))
              )
            )
        )
      } else {
        EmptyTag
      }
    }

    def deleteText(props: Props, state: State): Callback = Callback.future{
      val textToDelete = state.deleteText.get
      Ajax.post(
        url = props.deleteTextUrl,
        data = textToDelete.id.get.toString
      ).map(resp =>
        $.modState(_.copy(
          texts = if (resp.responseText == OK) removeById(state.texts, textToDelete) else state.texts,
          deleteText = None,
          waitPane = false
        ))
      )
    }
  }

  def removeById(list: List[TextUI], oldText: TextUI) = list.filterNot(_.id == oldText.id)
}
