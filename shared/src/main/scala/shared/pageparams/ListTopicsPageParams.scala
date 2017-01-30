package shared.pageparams

import shared.dto.Paragraph

case class ListTopicsPageParams(
                                headerParams: HeaderParams,
                                doActionUrl: String,
                                createParagraphUrl: String,
                                renameParagraphUrl: String,
                                createTopicUrl: String,
                                updateTopicUrl: String,
                                paragraphs: List[Paragraph]
                                )