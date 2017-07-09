package db

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import slick.jdbc.H2Profile.api._

import scala.collection.JavaConversions._

trait DbTestHelper extends FreeSpec with Matchers with BeforeAndAfterAll with ScalaFutures  {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(1, Seconds))

  protected val db = Database.forConfig("", ConfigFactory.parseMap(Map(
    "url" -> "jdbc:h2:mem:test1",
    "driver" -> "org.h2.Driver",
    "connectionPool" -> "disabled",
    "keepAliveConnection" -> "true"
  )))

  override protected def afterAll(): Unit = db.close()

  protected def notImplemented = fail("Not implemented")
}
