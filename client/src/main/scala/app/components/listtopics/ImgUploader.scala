package app.components.listtopics

import app.Utils
import app.components.forms.FormCommonParams
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{ReactComponentB, _}
import org.scalajs.dom.raw.FormData
import shared.SharedConstants._
import shared.dto.Topic
import shared.forms.{DataResponse, ErrorResponse}

import scala.util.{Failure, Success}

object ImgUploader {
  protected case class Props(globalScope: GlobalScope,
                             topic: Topic,
                             onChange: List[String] => Callback)

  protected case class State(images: List[String])

  def apply(globalScope: GlobalScope,
            topic: Topic,
            name: String)
           (implicit formParams: FormCommonParams)=
    comp(Props(
      globalScope = globalScope,
      topic = topic,
      onChange = imgs => formParams.onChange(
        formParams.formData.set(name, imgs.mkString(";"), formParams.transformations)
      ).void
    ))

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State(p.topic.images))
    .renderPS{($,props,state)=>
      def updateImages(newImages: List[String]) =
        $.modState(_.copy(images = newImages)) >> props.onChange(newImages)
      <.div(
        <.input.file(
          ^.value:="",
          ^.onChange ==> { (e: ReactEventI) =>
            val fd = new FormData()
            fd.append(FILE, e.target.files(0))
            fd.append(TOPIC_ID, props.topic.id.get)
            props.globalScope.openWaitPane >> Utils.post(url = props.globalScope.pageParams.uploadTopicFileUrl, data = fd){
              case Success(DataResponse(fileName)) =>
                updateImages(state.images:::fileName::Nil) >> props.globalScope.closeWaitPane
              case Success(ErrorResponse(str)) => props.globalScope.openOkDialog(s"Error uploading file: $str")
              case Failure(throwable) => props.globalScope.openOkDialog("Error: " + throwable.getMessage)
            }.void
          }
          ,^.accept := "image/*"
        )
        ,state.images.map{img=>
          ImageCmp(
            id = img,
            url = props.globalScope.pageParams.getTopicImgUrl + "/" + props.topic.id.get + "/" + img,
            onDelete = imgId => updateImages(state.images.filterNot(_ == imgId)),
            onUp = imgId =>
              if (state.images.head == imgId) {
                Callback.empty
              } else {
                val imgVec = state.images.toVector
                val idx = imgVec.indexOf(imgId)
                updateImages(imgVec.updated(idx, imgVec(idx - 1)).updated(idx - 1, imgId).toList)
              },
            onDown = imgId =>
              if (state.images.last == imgId) {
                Callback.empty
              } else {
                val imgVec = state.images.toVector
                val idx = imgVec.indexOf(imgId)
                updateImages(imgVec.updated(idx, imgVec(idx + 1)).updated(idx + 1, imgId).toList)
              }
          )
        }
      )
    }.build
}
