package readtext

class Word(val text: String, val hiddable: Boolean) {
    var selected: Boolean = false
    var mouseEntered: Boolean = false
    var hidden: Boolean = false

    private var userInput: Option[String] = None
    def setUserInput(userInput: String): Unit = {
        this.userInput = Some(userInput)
        userInputIsCorrect = None
    }

    def unsetUserInput(): Unit = {
        userInput = None
    }

    def getUserInput: Option[String] = userInput

    var awaitingUserInput: Boolean = false
    var userInputIsCorrect: Option[Boolean] = None
}
