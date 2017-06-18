package shared.forms

import shared.Validations._
import shared.dto.{Paragraph, Topic, TopicTag}


object Forms {
  type SubmitResponse[F,S] = Either[FormData[F],S]

  lazy val paragraphForm = new FormMethods[Paragraph] {
    val title = field(_.name)(nonEmpty)
    end
  }

  lazy val topicForm = new FormMethods[Topic] {
    val paragraphId = field(_.paragraphId)(nonEmpty)
    val title = field(_.title)(nonEmpty)
    val images = field(_.images)(none)
    end
  }

  lazy val tagForm = new FormMethods[TopicTag] {
    val value = field(_.value)(nonEmpty)
    end
  }
}
