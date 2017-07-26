package app.components.listtopics

import app.Utils
import app.components.WindowFunc
import app.components.forms.FormCommonParams
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.raw.{File, FormData}
import shared.SharedConstants._
import shared.dto.Topic
import shared.forms.FormField

import scala.util.{Failure, Success}

object ImgUploader {

  protected case class Props(windowFunc: WindowFunc, globalScope: ListTopicsPageGlobalScope,
                             topic: Topic,
                             onChange: List[String] => Callback)

  protected case class State(images: List[String])

  def apply[T, S](windowFunc: WindowFunc, globalScope: ListTopicsPageGlobalScope,
            topic: Topic,
            field: FormField[T, List[String]])
           (implicit formParams: FormCommonParams[T,S]) =
    comp(Props(
      windowFunc = windowFunc,
      globalScope = globalScope,
      topic = topic,
      onChange = formParams.valueWasChanged(field)
    ))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(p.topic.images))
    .renderBackend[Backend]
    .componentWillMount { $ =>
      $.props.globalScope.registerPasteListener($.props.topic.id.get, $.backend.uploadImage)
    }.componentWillUnmount{ $ =>
      $.props.globalScope.unregisterPasteListener($.props.topic.id.get)
    }.build

  protected class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) =
      <.div(
        <.input.file(
          ^.value := "",
          ^.onChange ==> { (e: ReactEventFromInput) => uploadImage(e.target.files(0)) }
          , ^.accept := "image/*"
        ),
        state.images.toVdomArray { img =>
          ImageCmp(
            id = img,
            url = props.globalScope.pageParams.getTopicImgUrl + "/" + props.topic.id.get + "/" + img,
            onDelete = imgId => updateImages(props, state.images.filterNot(_ == imgId)),
            // TODO: use function from LazyTreeNode
            onUp = imgId =>
              if (state.images.head == imgId) {
                Callback.empty
              } else {
                val imgVec = state.images.toVector
                val idx = imgVec.indexOf(imgId)
                updateImages(props, imgVec.updated(idx, imgVec(idx - 1)).updated(idx - 1, imgId).toList)
              },
            // TODO: use function from LazyTreeNode
            onDown = imgId =>
              if (state.images.last == imgId) {
                Callback.empty
              } else {
                val imgVec = state.images.toVector
                val idx = imgVec.indexOf(imgId)
                updateImages(props, imgVec.updated(idx, imgVec(idx + 1)).updated(idx + 1, imgId).toList)
              }
          )
        }
      )

    def updateImages(props: Props, newImages: List[String]) =
      $.modState(_.copy(images = newImages)) >> props.onChange(newImages)

    def uploadImage(file: File): Callback = {
      val props = $.props.runNow()
      val state = $.state.runNow()
      if (file == null) {
        println("file == null")
        Callback.empty
      } else {
        val fd = new FormData()
        fd.append(FILE, file)
        fd.append(TOPIC_ID, props.topic.id.get)
        props.windowFunc.openWaitPane >> Utils.post(url = props.globalScope.pageParams.uploadTopicFileUrl, data = fd) {
          case Success(Right(fileName)) =>
            updateImages(props, state.images ::: fileName :: Nil) >> props.windowFunc.closeWaitPane
          case Success(Left(str)) => props.windowFunc.openOkDialog(s"Error uploading file: $str")
          case Failure(throwable) => props.windowFunc.openOkDialog("Error: " + throwable.getMessage)
          case _ => ???
        }.void
      }
    }
  }

}