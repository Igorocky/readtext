package shared.pageparams

import shared.dto.Paragraph

case class ListTopicsPageParams(headerParams: HeaderParams,
                                paragraphs: List[Paragraph],
                                doActionUrl: String,
                                createParagraphUrl: String,
                                updateParagraphUrl: String,
                                deleteParagraphUrl: String,
                                createTopicUrl: String,
                                updateTopicUrl: String,
                                deleteTopicUrl: String,
                                uploadTopicFileUrl: String,
                                getTopicImgUrl: String) {
  lazy val eraseData = copy(paragraphs = null)
}
