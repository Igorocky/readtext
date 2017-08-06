package utils

import javax.inject.{Inject, Singleton}

import db.DaoCommon
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.utils.tree.LazyTreeNodeLike
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class Reconciliation @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                               val dao: DaoCommon,
                               val configuration: play.api.Configuration
                              )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

/*
  def reconcileFoldersFromParagraphs(rootParagraphId: Option[Long], rootFolderId: Option[Long]) = {
    val CREATE = 1
    val MOVE_TO_END = 2
    val UPDATE = 3
    val DO_NOTHING = 4
    val PARAGRAPH_ID_EQ = "paragraphId="
    db.run((
      for {
        pairs <- paragraphTable.filter(_.parentId === rootParagraphId)
          .joinFull(folderTable.filter(_.parentFolderId === rootFolderId))
          .on{case (par, fol) => fol.sourceId === (PARAGRAPH_ID_EQ + par.id)}
          .result
        actions = pairs.groupBy{
          case (Some(p), None) => CREATE
          case (Some(p), Some(f)) if (p.order != f.order || p.name != f.name) => UPDATE
          case (Some(p), Some(f)) => DO_NOTHING
          case (None, Some(f)) => MOVE_TO_END
        }
        _ <- DBIO.sequence(for {
          (id, name, order) <- actions(CREATE).map(_._1.get).map(p => (p.id.get, p.name, p.order))
        } yield folderTable += Folder(sourceId = PARAGRAPH_ID_EQ + id, name = name, order = order))
        _ <- DBIO.sequence(for {
          (fid, name, order) <- actions(UPDATE).map{case (Some(p), Some(f)) => (f.id.get, p.name, p.order)}
        } yield folderTable.filter(_.id === fid).map(f => (f.name, f.order)).update((name, order)))
      } yield ()
    ).transactionally)
  }
*/

  def createChanges[R <: LazyTreeNodeLike[R], C](cur: LazyTreeNodeLike[R], target: LazyTreeNodeLike[R],
                                                 matcher: (R, R) => Boolean,
                                                 comp: (R /*current*/ , R /*target*/ ) => Option[C],
                                                 delete: R => C, nev: R => C): List[C] = {
    val allCur = cur.findNodes(_ => true)
    val allTarg = target.findNodes(_ => true)
    val deletions = minus(allCur, allTarg, matcher).map(delete)
    val otherChanges = allTarg.flatMap { targNode =>
      allCur.find(matcher(targNode, _)) match {
        case None => Some(nev(targNode))
        case Some(curNode) => comp(curNode, targNode)
      }
    }
    deletions ::: otherChanges
  }

  private def minus[E](set1: List[E], set2: List[E], cmp: (E,E) => Boolean): List[E] =
    set1.foldLeft(Nil: List[E]){
      case (res, elem1) => if (set2.exists(cmp(_, elem1))) res else elem1::res
    }

//  private def fullJoin[E](set1: List[E], set2: List[E], cmp: (E,E) => Boolean): (List[E], List[(E,E)], List[E]) = {
//    set1.foldLeft((Nil:List[E], Nil:List[(E,E)], set2)){
//      case ((lft,mid,rht), e1) => find(e1, rht, cmp) match {
//        case None => (e1::lft, mid, rht)
//        case Some((x,y,rest)) => (lft, (x,y)::mid, rest)
//      }
//    }
//  }

  private def find[E](elem: E, list: List[E], cmp: (E,E) => Boolean): Option[(E,E,List[E])] = list match {
    case Nil => None
    case x::rest if cmp(elem, x) => Some(elem, x, rest)
    case x::rest => find(elem, rest, cmp) match {
      case None => None
      case Some((e1,e2,rst)) => Some(e1,e2,x::rst)
    }
  }

}
