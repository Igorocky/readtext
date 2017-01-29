package shared.pageparams

import shared.dto.Paragraph

case class ListTopicsPageParams(
                                headerParams: HeaderParams,
                                doActionUrl: String,
                                createParagraphUrl: String,
                                renameParagraphUrl: String,
                                paragraphs: List[Paragraph]
                                )