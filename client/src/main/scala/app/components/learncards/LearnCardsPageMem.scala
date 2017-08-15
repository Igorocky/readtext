package app.components.learncards

import app.components.listtopics.TopicTree

case class LearnCardsPageMem(activeTopics: TopicTree = TopicTree(children = None),
                             newTopics: TopicTree = TopicTree(children = None),
                             activationTimeReduction: Option[String] = None)