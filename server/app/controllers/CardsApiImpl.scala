package controllers

import javax.inject._

import app.RouterBuilderUtils
import db.DaoCommon
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import shared.api.CardsApi
import shared.dto.CardLearnInfo
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CardsApiImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                             val dao: DaoCommon,
                             val configuration: play.api.Configuration
                            )(implicit private val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with RouterBuilderUtils[CardsApi] {

  var questionCnt = 0

  val router: Router = RouterBuilder()

    .addHandler(forMethod(_.loadNextCardInfo)) {
      case poolId =>
        questionCnt += 1
        Future.successful(CardLearnInfo(
          cardId = questionCnt, question = s"Question#$questionCnt", answer = s"Answer#$questionCnt"
        ))
    }

    .addHandler(forMethod3(_.scoreSelected)) {
      case (cardId, easiness, score) => Future.successful(())
    }
}
