package shared.dto

import java.time.ZonedDateTime

import shared.utils.Enum

case class Card(id: Option[Long] = None,
                sourceId: String,
                folderId: Long,
                questionType: QuestionType,
                answerType: AnswerType,
                created: ZonedDateTime,
                modified: ZonedDateTime,
                order: Int
               )

case class QuestionType(id: Int)
case class AnswerType(id: Int)

object QuestionTypes extends Enum[QuestionType] {
  private def questionType(id: Int) = addElem(QuestionType(id))

  val text = questionType(1)
  end
}

object AnswerTypes extends Enum[AnswerType] {
  private def answerType(id: Int) = addElem(AnswerType(id))

  val topic = answerType(1)
  end
}
