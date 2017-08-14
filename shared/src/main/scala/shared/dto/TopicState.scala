package shared.dto

case class TopicState(timeOfChange: String,
                      lastChangedDuration: String,
                      score: String,
                      comment: String,
                      activationTime: String,
                      isActive: Boolean,
                      timeLeftUntilActivation: Option[String],
                      timePassedAfterActivation: Option[String]
                     )