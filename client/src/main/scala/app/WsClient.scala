package app

import japgolly.scalajs.react.Callback

trait WsClient[A] extends WsClientMacro[A, Callback]