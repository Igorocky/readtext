package app.components.listtopics

import app.Utils._
import app.components.forms.{FormCommonParams, FormTextField}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}
import shared.FormKeys
import shared.forms.{FormData, Forms}
import upickle.default._

object TagsCmp {

  protected case class Props(globalScope: GlobalScope,
                             addTagUrl: String,
                             entityId: Long,
                             tags: List[String],
                             removeTag: String => Callback,
                             tagAdded: List[String] => Callback)

  protected case class State(formData: Option[FormData] = None)


  def apply(globalScope: GlobalScope,
            addTagUrl: String, entityId: Long,
            tags: List[String], removeTag: String => Callback, tagAdded: List[String] => Callback) =
    comp(Props(globalScope, addTagUrl, entityId, tags, removeTag, tagAdded))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderPS{ ($,props,state) =>
      <.span(^.`class` := this.getClass.getSimpleName,
        if (state.formData.isEmpty) {
          buttonWithIcon(
            onClick = $.modState(_.copy(
              formData = Some(Forms.tagForm.formData(
                language = props.globalScope.pageParams.headerParams.language,
                obj = (props.entityId, ""),
                submitUrl = props.addTagUrl
              ))
            )),
            btnType = BTN_INFO,
            iconType = "fa-hashtag"
          )
        } else {
          implicit val fParams = FormCommonParams(
            id = "tag-form",
            formData = state.formData.get,
            transformations = Forms.tagForm.transformations,
            onChange = fd => $.modState(_.copy(formData = Some(fd))).map(_ => fd),
            submitUrl = state.formData.get.submitUrl,
            beforeSubmit = props.globalScope.openWaitPane,
            onSubmitSuccess =
              str => props.globalScope.closeWaitPane >>
                props.tagAdded(read[List[String]](str)) >>
                $.modState(_.copy(formData = None)),
            onSubmitFormCheckFailure = props.globalScope.closeWaitPane,
            onAjaxError = th => props.globalScope.openOkDialog(s"""Error: ${th.getMessage}"""),
            editMode = false
          )
          FormTextField(
            key = FormKeys.TAG,
            focusOnMount = true,
            onEscape = $.modState(_.copy(formData = None))
          )
        },
        props.tags.toVdomArray(tag =>
          <.span(
            ^.key:=tag,
            ^.`class`:="tag",
            <.span(
              ^.`class`:="tag-text",
              tag
            ),
            <.span(
              ^.`class`:="rem-tag-btn",
              "x",
              ^.onClick --> props.removeTag(tag)
            )
          )
        )
      )
    }.build

}