package app

import org.scalatest.{FlatSpec, Matchers}

class WsMacrosTest extends FlatSpec with Matchers {
  trait Api {
    def updateTopic(id: Long, data: String): List[String]
    def updateParagraph(id: Long): Either[String, Int]
  }

  "WsClient.post" should "do correct post call" in {
    var called = false
    val client = new WsClient[Api, String] {
      override def doCall[O](path: String,
                             dataStr: String,
                             reader: (String) => O,
                             errHnd: Throwable => String): (O => String) => String = {
        called = true
        path should be("WsMacrosTest.this.Api.updateTopic")
        dataStr shouldBe ("""["78","eeeee"]""")
        fn => fn(reader(""))
      }
    }

    client.post(_.updateTopic(78, "eeeee"), th => "th.")
    called should be(true)
  }

  "WsClient.post" should "invoke error handler" in {
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

  "WsClient.post" should "return correct result" in {
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

  "WsClient.forMethod" should "return correct result for a method with two arguments" in {
    val server = new RouterBuilderUtils[Api] {
      val result = forMethod2(_.updateTopic)
    }

    val (path, reader, writer) = server.result
    path should be(("WsMacrosTest.this.Api.updateTopic"))
    reader("""["48","s478"]""") should be((48L,"s478"))
    writer(List("A1","b2","c-")) should be("""["A1","b2","c-"]""")
  }

  "WsClient.forMethod" should "return correct result for a method with one argument" in {
    val server = new RouterBuilderUtils[Api] {
      val result = forMethod(_.updateParagraph)
    }

    val (path, reader, writer) = server.result
    path should be(("WsMacrosTest.this.Api.updateParagraph"))
    reader(""""48"""") should be(48L)
    writer(Right(49)) should be("[1,49]")
  }
}
