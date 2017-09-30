package app.components.listtopics

import app.WsClient
import app.components.WindowFunc
import app.components.forms.{FormCommonParams, FormTextField}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.api.CardsApi
import shared.forms.{FormData, Forms}
import shared.messages.Language

trait ScoreCmpActions {
  def wf: WindowFunc
  def cardsApi: WsClient[CardsApi]
  def updateCardState(commentAndScore: String, cardId: Long) = cardsApi.post(
    _.updateCardState(cardId, commentAndScore),
    th => wf.openOkDialog("Error saving score: " + th.getMessage)
  )
  def cardStateUpdated(cardId: Long): Callback
}

object ScoreCmp {

  case class Props(ctx: WindowFunc with ScoreCmpActions,
                   cardId: Long,
                   language: Language) {
    @inline def render = comp(this)
  }

  protected case class State(formData: FormData[String]) {
    def clearInputField = copy(formData = formData.copy(data = ""))
  }


  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(FormData(p.language, "")))
    .renderPS{ ($,props,state) =>
      val formMethods = Forms.scoreForm
      implicit val fParams = FormCommonParams[String, Unit](
        formData = state.formData,
        formMethods = formMethods,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        beforeSubmit = props.ctx.openWaitPane,
        submitFunction = props.ctx.updateCardState(_, props.cardId),
        onSubmitSuccess =
          _ => props.ctx.closeWaitPane >> props.ctx.cardStateUpdated(props.cardId) >> $.modState(_.clearInputField),
        onSubmitFormCheckFailure = props.ctx.closeWaitPane,
        editMode = false
      )
      <.div(^.`class` := this.getClass.getSimpleName,
        FormTextField(
          field = formMethods.value,
          onEscape = $.modState(_.clearInputField),
          placeholder = "Comment and score",
          width = 300
        )
      )
    }.build

}