package app.components.listtopics

import app.components.Button
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}

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
    comp.withKey(id)(Props(id,
      url,
      onDelete,
      onUp,
      onDown))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State())
    .renderPS{($,props,state)=>
      <.div(
        Button(id = props.id + "-remove-btn", name = "X", onClick = props.onDelete(props.id))
        ,Button(id = props.id + "-up-btn", name = "up", onClick = props.onUp(props.id))
        ,Button(id = props.id + "-down-btn", name = "down", onClick = props.onDown(props.id))
        ,<.img(
          ^.src:=props.url
        )
      )
    }.build
}
