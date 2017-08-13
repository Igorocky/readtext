package app.components.learncards

import shared.dto.{Topic, TopicState}

case class LearnCardsPageMem(topic: Option[Topic] = None,
                             topicStates: Option[List[TopicState]] = None)