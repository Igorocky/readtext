package reconcile

import javax.inject._

import db.Tables._
import db.{DaoCommon, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.dto.{Card, Folder}
import shared.utils.tree.LazyTreeNodeLike
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

case class DeleteCard(card: Card)
case class ChangeCard(srcCard: Card, dstCard: Card)
case class CreateCard(card: Card)

case class DeleteFolder(folder: Folder)
case class ChangeFolder(srcFolder: Folder, dstFolder: Folder)
case class CreateFolder(folder: Folder)

case class CardTree(value: Option[Any] = None, children: Option[List[CardTree]] = None) extends LazyTreeNodeLike[CardTree] {
  override def setChildren(newChildren: Option[List[CardTree]]): CardTree = copy(children = newChildren)
  override def setValue(newValue: Option[Any]): CardTree = copy(value = newValue)

  def loadFromDb()(implicit db: JdbcBackend#DatabaseDef) = {

  }
}

@Singleton
class TopicCardsRecons @Inject()(val dao: DaoCommon,
                                 protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

//  def loadParagraphs(parentId: Option[Long]): Future[List[CardTree]] = db.run(for {
//    pars <- dao.loadOrderedChildren(paragraphTable, parentId)
//    par <- pars
//  } yield CardTree(value = Some(par)))
//
//  def loadFolders(parentFolderId: Option[Long]): Future[List[CardTree]] = db.run(for {
//    folders <- dao.loadOrderedChildren(folderTable, parentFolderId)
//    folder <- folders
//  } yield CardTree(value = Some(folder)))
}
