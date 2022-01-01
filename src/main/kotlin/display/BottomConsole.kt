package display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import displaySpacing
import game.Message

/**
 * The GUI for the MessageLog at the bottom of the screen. Appends new messages to the top, with the turn number in
 * brackets next to each message. Scrollable.
 */
@Composable
fun BottomConsole(
    messages: List<Message>
) {
    LazyColumn(
        modifier = Modifier
            //.fillMaxWidth()
            .background(DarkGray)
    ) {
        messages
            .reversed()
            .forEach { msg ->
                item {
                    Text("[${msg.turn}] ${msg.text}", color = msg.textColor, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(displaySpacing))
                }
            }
    }
}