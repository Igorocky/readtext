package app.components.listtopics

import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, _}
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.FormData
import shared.SharedConstants._
import shared.dto.Topic

object FileUploader {
  protected case class Props(globalScope: GlobalScope,
                             topic: Topic)

  protected case class State()

  def apply(globalScope: GlobalScope,
            topic: Topic) =
    comp(Props(
      globalScope,
      topic
    ))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State())
    .renderPS{($,props,state)=>
      <.div(
        <.input.file(
          ^.name := "file111",
          ^.onChange ==> {(e: ReactEventI) =>
            println("e === " + e.target.value)
            val fd = new FormData()
            fd.append(FILE, e.target.files(0))
            fd.append(PARAGRAPH_ID, props.topic.paragraphId.get)
            fd.append(TOPIC_ID, props.topic.id.get)
            Ajax.post(url = props.globalScope.pageParams.uploadTopicFileUrl, data = fd)
            Callback.empty
          }
        )
      )
    }.build
}
