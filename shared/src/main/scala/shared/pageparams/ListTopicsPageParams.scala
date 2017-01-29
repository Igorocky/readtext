package shared.pageparams

import shared.dto.Paragraph

case class ListTopicsPageParams(
                                headerParams: HeaderParams,
                                doActionUrl: String,
                                paragraphs: List[Paragraph]
                                )