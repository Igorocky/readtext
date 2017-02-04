package app.components.listtopics

import app.Utils
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, _}
import org.scalajs.dom.raw.FormData
import shared.SharedConstants._
import shared.dto.Topic
import shared.forms.DataResponse
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.util.{Failure, Success}

object FileUploader {
  protected case class Props(globalScope: GlobalScope,
                             topic: Topic)

  protected case class State(img: Option[String] = None)

  def apply(globalScope: GlobalScope,
            topic: Topic) =
    comp(Props(
      globalScope,
      topic
    ))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State())
    .renderPS{($,props,state)=>
      if (state.img.isEmpty) {
        <.div(
          <.input.file(
            ^.name := "file111",
            ^.onChange ==> { (e: ReactEventI) =>
              println("e === " + e.target.value)
              val fd = new FormData()
              fd.append(FILE, e.target.files(0))
              fd.append(TOPIC_ID, props.topic.id.get)
              Callback.future(Utils.post(url = props.globalScope.pageParams.uploadTopicFileUrl, data = fd).map {
                case Success(DataResponse(fileName)) => $.modState(_.copy(img = Some(fileName)))
                case Failure(throwable) => props.globalScope.openOkDialog("Error: " + throwable.getMessage)
              })
            },
            ^.accept := "image/*"
          )
        )
      } else {
        <.img(^.src:=s"${props.globalScope.pageParams.getTopicImgUrl}/${props.topic.id.get}/${state.img.get}")
      }
    }.build
}
