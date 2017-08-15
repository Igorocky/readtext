package app.components.learncards

import app.components.listtopics.TopicTree
import shared.dto.Topic

case class LearnCardsPageMem(activeTopics: TopicTree = TopicTree(children = None),
                             activationTimeReduction: Option[String] = None)