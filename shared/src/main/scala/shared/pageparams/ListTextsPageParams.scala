package shared.pageparams

case class ListTextsPageParams(
                                headerParams: HeaderParams,
                                loadFullTextUrl: String,
                                mergeTextUrl: String,
                                deleteTextUrl: String,
                                texts: List[TextUI]
                                )