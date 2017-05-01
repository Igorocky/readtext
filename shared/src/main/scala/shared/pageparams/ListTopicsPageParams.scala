package shared.pageparams

import shared.dto.Paragraph

case class ListTopicsPageParams(headerParams: HeaderParams,
                                paragraphs: List[Paragraph],
                                createParagraphUrl: String,
                                updateParagraphUrl: String,
                                deleteParagraphUrl: String,
                                createTopicUrl: String,
                                updateTopicUrl: String,
                                deleteTopicUrl: String,
                                uploadTopicFileUrl: String,
                                getTopicImgUrl: String,
                                expandUrl: String,
                                moveUpParagraphUrl: String,
                                moveUpTopicUrl: String,
                                moveDownParagraphUrl: String,
                                moveDownTopicUrl: String,
                                checkParagraphUrl: String,
                                checkTopicsUrl: String,
                                addTagForTopicUrl: String,
                                removeTagFromTopicUrl: String) {
  lazy val eraseData = copy(paragraphs = null)
}
