package db

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FreeSpec, Matchers}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.H2Profile.api._
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.collection.JavaConversions._

trait DbTestHelper extends FreeSpec with Matchers with BeforeAndAfterAll with ScalaFutures /*with HasDatabaseConfigProvider[JdbcProfile]*/ {

  implicit override val patienceConfig = PatienceConfig(timeout = Span(3, Seconds))

//  protected val db = Database.forConfig("", ConfigFactory.parseMap(Map(
//    "url" -> "jdbc:h2:mem:test1",
//    "driver" -> "org.h2.Driver",
//    "connectionPool" -> "disabled",
//    "keepAliveConnection" -> "true"
//  )))

  private val dbConf: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("", ConfigFactory.parseMap(Map(
    "profile" -> "slick.jdbc.H2Profile$",
    "db.url" -> "jdbc:h2:mem:test1",
    "db.driver" -> "org.h2.Driver",
    "db.connectionPool" -> "disabled",
    "db.keepAliveConnection" -> "true"
  )))

  protected val dbConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] = dbConf.asInstanceOf[DatabaseConfig[P]]
  }

  protected val db = dbConf.db

  override protected def afterAll(): Unit = db.close()

  protected def notImplemented = fail("Not implemented")
}
