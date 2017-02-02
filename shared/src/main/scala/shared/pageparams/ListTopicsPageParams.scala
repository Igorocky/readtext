package shared.pageparams

import shared.dto.Paragraph

case class ListTopicsPageParams(headerParams: HeaderParams,
                                paragraphs: List[Paragraph],
                                doActionUrl: String,
                                createParagraphUrl: String,
                                renameParagraphUrl: String,
                                createTopicUrl: String,
                                updateTopicUrl: String,
                                uploadTopicFileUrl: String) {
  lazy val eraseData = copy(paragraphs = null)
}
