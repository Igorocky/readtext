package app.components.listtopics

import app.Utils.{BTN_DANGER, BTN_INFO, buttonWithIcon}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}

object ImageCmp {
  type ID = String
  protected case class Props(id: ID,
                             url: String,
                             onDelete: ID => Callback,
                             onUp: ID => Callback,
                             onDown: ID => Callback)

  protected case class State()

  def apply(id: ID,
            url: String,
            onDelete: ID => Callback,
            onUp: ID => Callback,
            onDown: ID => Callback) =
    comp.withKey(id.toString)(Props(id,
      url,
      onDelete,
      onUp,
      onDown))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State())
    .renderPS{($,props,state)=>
      <.div(
        buttonWithIcon(onClick = props.onDelete(props.id), btnType = BTN_DANGER, iconType = "fa-trash-o")
        ,buttonWithIcon(onClick = props.onUp(props.id), btnType = BTN_INFO, iconType = "fa-long-arrow-up")
        ,buttonWithIcon(onClick = props.onDown(props.id), btnType = BTN_INFO, iconType = "fa-long-arrow-down")
        ,<.img(
          ^.src:=props.url
        )
      )
    }.build
}
