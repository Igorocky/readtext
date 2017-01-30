package app.components.listtopics

import app.components.forms.{FormCommonParams, FormTextField, SubmitButton}
import app.components.{Button, WaitPane}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, ReactComponentB, ReactEventAliases}
import shared.SharedConstants
import shared.dto.Topic
import shared.forms.Forms
import shared.messages.Language
import upickle.default._
import japgolly.scalajs.react._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.FormData

object FileUploader {
  protected case class Props()

  protected case class State()

  def apply() =
    comp(Props())

  private lazy val comp = ReactComponentB[Props](this.getClass.getName)
    .initialState_P(p => State())
    .renderPS{($,props,state)=>
      <.div(
        <.input.file(
          ^.name := "file111",
          ^.onChange ==> {(e: ReactEventI) =>
            println("e === " + e.target.value)
            val fd = new FormData()
            fd.append("fileQQWWEE", e.target.files(0))
            Ajax.post(url = "/uploadFile/DFGH", data = fd)
            Callback.empty
          }
        )
      )
    }.build
}
