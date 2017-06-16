package shared.pageparams

case class ListTopicsPageParams(headerParams: HeaderParams,
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
                                addTagForTopicUrl: String,
                                removeTagFromTopicUrl: String,
                                wsEntryUrl: String
                               )
