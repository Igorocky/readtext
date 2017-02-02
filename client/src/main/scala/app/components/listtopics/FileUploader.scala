package app.components.listtopics

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, _}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.FormData

object FileUploader {
  protected case class Props(globalScope: GlobalScope)

  protected case class State()

  def apply(globalScope: GlobalScope) =
    comp(Props(globalScope))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State())
    .renderPS{($,props,state)=>
      <.div(
        <.input.file(
          ^.name := "file111",
          ^.onChange ==> {(e: ReactEventI) =>
            println("e === " + e.target.value)
            val fd = new FormData()
            fd.append("file", e.target.files(0))
            Ajax.post(url = props.globalScope.pageParams.uploadTopicFileUrl, data = fd)
            Callback.empty
          }
        )
      )
    }.build
}
