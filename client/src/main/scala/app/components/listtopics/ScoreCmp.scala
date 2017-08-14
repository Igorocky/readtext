package app.components.listtopics

import app.components.WindowFunc
import app.components.forms.{FormCommonParams, FormTextField}
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import shared.forms.{FormData, Forms}

object ScoreCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext,
                   entityId: Long) {
    @inline def render = comp(this)
  }

  protected case class State(formData: FormData[String])


  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(FormData(p.ctx.language, "")))
    .renderPS{ ($,props,state) =>
      val formMethods = Forms.scoreForm
      implicit val fParams = FormCommonParams[String, Unit](
        formData = state.formData,
        formMethods = formMethods,
        onChange = fd => $.modState(_.copy(formData = fd)).map(_ => fd),
        beforeSubmit = props.ctx.openWaitPane,
        submitFunction = commentAndScore => props.ctx.cardsClient.post(
          _.updateCardState(props.entityId, commentAndScore),
          th => props.ctx.openOkDialog("Error saving score: " + th.getMessage)
        ),
        onSubmitSuccess =
          _ => props.ctx.closeWaitPane >> props.ctx.topicStateUpdated(props.entityId) >>
            $.modState(s => s.copy(formData = s.formData.copy(data = ""))),
        onSubmitFormCheckFailure = props.ctx.closeWaitPane,
        editMode = false
      )
      <.div(^.`class` := this.getClass.getSimpleName,
        FormTextField(
          field = formMethods.value,
          onEscape = $.modState(s => s.copy(formData = s.formData.copy(data = ""))),
          placeholder = "Comment and score",
          width = 300
        )
      )
    }.build

}