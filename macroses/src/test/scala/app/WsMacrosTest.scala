package app

import org.scalatest.{FlatSpec, Matchers}

class WsMacrosTest extends FlatSpec with Matchers {
  "WsClient" should "do correct post call" in {
    var called = false
    val client = new WsClient[Api, String] {
      override def doCall[O](path: String,
                             dataStr: String,
                             reader: (String) => O,
                             errHnd: Throwable => String): (O => String) => String = {
        called = true
        path should be("app.Api.updateTopic")
        dataStr shouldBe ("""["78","eeeee"]""")
        fn => fn(reader(""))
      }
    }

    client.post(_.updateTopic(78, "eeeee"), th => "th.")
    called should be(true)
  }

  "WsClient" should "invoke error handler" in {
    var called = false
    val throwable = new Throwable

    val client = new WsClient[Api, String] {
      override def doCall[O](path: String,
                             dataStr: String,
                             reader: (String) => O,
                             errHnd: Throwable => String): (O => String) => String = {
        errHnd(throwable)
        fn => fn(reader(""))
      }
    }

    client.post(_.updateTopic(78, "eeeee"), th => {
      th should be(throwable)
      called = true
      ""
    })
    called should be(true)
  }

  "WsClient" should "return correct result" in {
    val client = new WsClient[Api, String] {
      override def doCall[O](path: String,
                             dataStr: String,
                             reader: (String) => O,
                             errHnd: Throwable => String): (O => String) => String = {
        fn => fn(reader("""["159","160"]"""))
      }
    }

    val result = client.post(_.updateTopic(78, "eeeee"), _ => "")(_.mkString("<",";",">"))
    result should be("<159;160>")
  }

}
