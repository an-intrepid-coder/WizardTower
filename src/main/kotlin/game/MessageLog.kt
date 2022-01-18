package game

import androidx.compose.ui.graphics.Color

/**
 * Contains a turn-stamped, colored message.
 */
data class Message(
    val turn: Int,
    val text: String,
    val textColor: Color,
    // more to come
)

/**
 * Just a wrapper over a MutableList<Message> for now. May do something more complex here down the road.
 */
class MessageLog {
    val messages = mutableListOf<Message>()

    /**
     * Adds a message to the log.
     */
    fun addMessage(msg: Message) {
        messages.add(msg)
    }
}