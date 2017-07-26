package app.components

import app.Utils.{BTN_DEFAULT, BTN_PRIMARY, buttonWithText}
import app.WsClient
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.api.SessionApi
import shared.messages.Language

object UnivPage {

  case class Props(language: Language,
                   sessionWsClient: WsClient[SessionApi],
                   onLanguageChange: Language => Callback,
                   content: TagMod,
                   windowFuncMem: WindowFuncMem,
                   windowFunc: WindowFunc) {
    @inline def render = comp(this)
  }

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .renderBackend[Backend]
    .build

  protected class Backend($: BackendScope[Props, Unit]) {
    def render(implicit props: Props) =
      <.div(
        <.div(
          LanguageSelector(
            currLang = props.language
            ,sessionWsClient = props.sessionWsClient
            ,onChange = props.onLanguageChange
          )
        ),
        <.div(
          props.content
        ),
        waitPaneIfNecessary,
        okDialogIfNecessary,
        okCancelDialogIfNecessary
      )

    def waitPaneIfNecessary(implicit props: Props): TagMod =
      if (props.windowFuncMem.waitPane) {
        if (props.windowFuncMem.okDiagText.isDefined || props.windowFuncMem.okCancelDiagText.isDefined) WaitPane() else WaitPane("rgba(255,255,255,0.0)")
      } else EmptyVdom

    def okDialogIfNecessary(implicit props: Props): TagMod =
      props.windowFuncMem.okDiagText.whenDefined(text=>
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(props.windowFuncMem.okDiagText.get),
            buttonWithText(
              onClick = props.windowFunc.closeOkDialog,
              btnType = BTN_PRIMARY,
              text = "OK"
            )
          )
        )
      )

    def okCancelDialogIfNecessary(implicit props: Props): TagMod =
      props.windowFuncMem.okCancelDiagText.whenDefined(text =>
        ModalDialog(
          width = "400px",
          content = <.div(
            <.div(text),
            <.div(
              buttonWithText(
                onClick = props.windowFunc.closeOkCancelDialog >> props.windowFuncMem.onOk,
                btnType = BTN_PRIMARY,
                text = "OK"
              ),
              buttonWithText(
                onClick = props.windowFunc.closeOkCancelDialog >> props.windowFuncMem.onCancel,
                btnType = BTN_DEFAULT,
                text = "Cancel"
              )
            )
          )
        )
      )

  }
}



case class WindowFuncMem(waitPane: Boolean = false,
                         okDiagText: Option[String] = None,
                         okCancelDiagText: Option[String] = None,
                         onOk: Callback = Callback.empty,
                         onCancel: Callback = Callback.empty)

trait WindowFunc {
  protected def modWindowFuncMem(f: WindowFuncMem => WindowFuncMem): Callback

  private def mod(f: WindowFuncMem => WindowFuncMem): Callback = modWindowFuncMem(f)

  def openOkDialog(text: String): Callback = openWaitPane >> mod(_.copy(okDiagText = Some(text)))

  def closeOkDialog: Callback = mod(_.copy(okDiagText = None)) >> closeWaitPane

  def openWaitPane: Callback = mod(_.copy(waitPane = true))

  def closeWaitPane: Callback = mod(_.copy(waitPane = false))

  def openOkCancelDialog(text: String, onOk: Callback, onCancel: Callback): Callback =
    openWaitPane >> mod(_.copy(okCancelDiagText = Some(text), onOk = onOk, onCancel = onCancel))

  def openOkCancelDialog(text: String, onOk: Callback): Callback =
    openWaitPane >> mod(_.copy(okCancelDiagText = Some(text), onOk = onOk, onCancel = Callback.empty))

  def closeOkCancelDialog: Callback =
    mod(_.copy(okCancelDiagText = None, onOk = Callback.empty, onCancel = Callback.empty)) >> closeWaitPane

  def showError(throwable: Throwable): Callback = openOkDialog("Error: " + throwable)


}