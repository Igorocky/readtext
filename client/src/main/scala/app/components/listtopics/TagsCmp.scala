package app.components.listtopics

import app.Utils._
import app.components.WindowFunc
import app.components.forms.FormCommonParams.SubmitFunction
import app.components.forms.{FormCommonParams, FormTextField}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.SharedConstants._
import shared.dto.TopicTag
import shared.forms.{FormData, Forms}

object TagsCmp {

  case class Props(ctx: WindowFunc with ListTopicsPageContext,
                   readOnly: Boolean = false,
                   submitFunction: SubmitFunction[TopicTag, List[String]],
                   entityId: Long,
                   tags: List[String],
                   removeTag: String => Callback,
                   tagAdded: List[String] => Callback) {
    @inline def render = comp(this)
  }

  protected case class State(formData: Option[FormData[TopicTag]] = None)


  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderPS{ ($,props,state) =>
      <.span(^.`class` := this.getClass.getSimpleName,
        if (!props.readOnly) {
          if (state.formData.isEmpty) {
            buttonWithIcon(
              onClick = $.modState(_.copy(
                formData = Some(FormData(props.ctx.language, TopicTag(props.entityId)))
              )),
              btnType = BTN_INFO,
              iconType = "fa-hashtag"
            )
          } else {
            val formMethods = Forms.tagForm
            implicit val fParams = FormCommonParams[TopicTag, List[String]](
              formData = state.formData.get,
              formMethods = formMethods,
              onChange = fd => $.modState(_.copy(formData = Some(fd))).map(_ => fd),
              beforeSubmit = props.ctx.openWaitPane,
              submitFunction = tag => props.ctx.wsClient.post(
                _.addTagForTopic(tag),
                th => props.ctx.openOkDialog("Error adding tag: " + th.getMessage)
              ),
              onSubmitSuccess =
                tags => props.ctx.closeWaitPane >>
                  props.tagAdded(tags) >>
                  $.modState(_.copy(formData = None)),
              onSubmitFormCheckFailure = props.ctx.closeWaitPane,
              editMode = false
            )
            FormTextField(
              field = formMethods.value,
              focusOnMount = true,
              onEscape = $.modState(_.copy(formData = None))
            )
          }
        } else EmptyVdom,
        props.tags.toVdomArray(tag =>
          <.span(^.key:=tag, ^.`class`:=TAG,
            <.span(^.`class`:=TAG_TEXT,
              tag
            ),
            if (!props.readOnly){
              <.span(^.`class`:=REM_TAG_BTN,
                "x",
                ^.onClick --> props.removeTag(tag)
              )
            } else " "
          )
        )
      )
    }.build

}