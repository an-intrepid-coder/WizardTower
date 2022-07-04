import game.WizardTowerGame
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import inputoutput.WizardTowerInterface

// The game is the "back end" of the application:
val game = WizardTowerGame()

fun main() = application {
    // The Composable Interface is the "front end" of the application:
    Window(
        onCloseRequest = ::exitApplication,
        title = "Wizard Tower",
        state = rememberWindowState(width = 700.dp, height = 850.dp),
        onKeyEvent = { keyEvent ->
            // For now, the input system responds only to KeyUp events -- I will expand the input system soon.
            if (keyEvent.type == KeyEventType.KeyUp && !game.inputLocked)
                game.handleInput(keyEvent)
            true
        }
    ) {
        MaterialTheme { // TODO: Custom MaterialTheme, changing up some font stuff.
                        //       Should auto-apply to the whole app from there.
            WizardTowerInterface(game)
        }
    }
}