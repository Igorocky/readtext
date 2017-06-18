package controllers

import java.io.File
import javax.inject._

import app.RouterBuilderUtils
import db.Tables._
import db.TypeConversions._
import db.{DaoCommon, HasIdAndOrder}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.api.TopicApi
import shared.forms.Forms
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

@Singleton
class TopicApiImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             val dao: DaoCommon,
                             val configuration: play.api.Configuration
                            )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with RouterBuilderUtils[TopicApi] {

  val router: Router = RouterBuilder()

    .addHandler(forMethod(_.loadTopicsByParentId)) {
      case parentId => db.run(
        dao.loadOrderedChildren(topicTable, parentId)
      ).map(_.toList)
    }

    .addHandler(forMethod(_.loadParagraphsByParentId)) {
      case optParentId => db.run(
        dao.loadOrderedChildren(paragraphTable, optParentId)
      ).map(_.toList)
    }

    .addHandlerOfFormSubmit(forMethod(_.createParagraph))(Forms.paragraphForm){
      case paragraph => db.run(
        dao.insertOrdered(paragraphTable)(
          parentId = 0,
          updateOrder = ord => paragraph.copy(order = ord),
          updateId = (par, newId) => par.copy(id = Some(newId))
        )
      )
    }

    .addHandlerOfFormSubmit(forMethod(_.updateParagraph))(Forms.paragraphForm){
      case paragraph => db.run(
        dao.updateField(paragraphTable)(paragraph.id.get, _.name)(_ => paragraph.name)
      ).map(_ => paragraph)
    }

    .addHandlerOfFormSubmit(forMethod(_.createTopic))(Forms.topicForm){
      case topic => db.run(
        dao.insertOrdered(topicTable)(
          parentId = topic.paragraphId.get,
          updateOrder = ord => topic.copy(order = ord),
          updateId = (top, newId) => top.copy(id = Some(newId))
        )
      )
    }

    .addHandlerOfFormSubmit(forMethod(_.updateTopic))(Forms.topicForm){
      case topic => db.run(
        topicTable.filter(_.id === topic.id).map(t => (t.title, t.images)).update((topic.title, topic.images))
      ).map{_ =>
        removeUnusedImages(topic.id.get, topic.images)
        topic
      }
    }

    .addHandlerOfFormSubmit(forMethod(_.addTagForTopic))(Forms.tagForm){
      case tag => db.run(
        dao.updateField(topicTable)(tag.parentId, _.tags)(tag.value::_)
      )
    }





    .addHandler(forMethod(_.deleteParagraph)) {
      case id => db.run(
        dao.deleteOrdered(paragraphTable, id)
      )
    }
    .addHandler(forMethod(_.deleteTopic)) {
      case id => db.run(
        dao.deleteOrdered(topicTable, id)
      )
    }
    .addHandler(forMethod(_.expand)) {
      case ids =>
        val trueIds = ids.filter(_._2).map(_._1)
        val falseIds = ids.filter(!_._2).map(_._1)
        db.run(
          DBIO.seq(
            paragraphTable.filter(_.id inSet trueIds).map(_.expanded).update(true),
            paragraphTable.filter(_.id inSet falseIds).map(_.expanded).update(false)
          )
        )
    }
    .addHandler(forMethod(_.moveUpParagraph)) {
      case id => changeOrder(id, paragraphTable, false)
    }
    .addHandler(forMethod(_.moveUpTopic)) {
      case id => changeOrder(id, topicTable, false)
    }
    .addHandler(forMethod(_.moveDownParagraph)) {
      case id => changeOrder(id, paragraphTable, true)
    }
    .addHandler(forMethod(_.moveDownTopic)) {
      case id => changeOrder(id, topicTable, true)
    }
    .addHandler(forMethod2(_.removeTagFromTopic)) {
      case (topId, tagToRemove) => db.run(
        dao.updateField(topicTable)(topId, _.tags)(_.filterNot(_ == tagToRemove))
      )
    }


  private def removeUnusedImages(topicId: Long, images: List[String]): Unit = {
    Some(getTopicImagesDir(topicId, imgStorageDir).listFiles()).filterNot(_ eq null).foreach { files =>
      files.filterNot(f => images.exists(_ == f.getName))
        .foreach(_.delete())
    }
  }

  private def getTopicImagesDir(topicId: Long, imgStorageDir: File): File =
    new File(imgStorageDir, s"/$topicId")

  private val imgStorageDir = new File(configuration.getString("topicsImgStorageDir").get)

  private def changeOrder[M <: HasIdAndOrder :ClassTag,U,C[_]](id: Long, table: Query[M,U,C], down: Boolean) =
    db.run(
      dao.changeOrder(table, id, down)
    ).map(_ => ())

}
