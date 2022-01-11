package inputoutput

import androidx.compose.foundation.background
import game.WizardTowerGame
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import game.InputMode

val displaySpacing = 8.dp

/**
 * The main interface for Wizard Tower. Includes a scrolling camera, a side-hud, and a bottom console.
 */
@Composable
fun WizardTowerInterface(
    game: WizardTowerGame,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGray),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.height(displaySpacing))
        Row {
            Spacer(Modifier.width(displaySpacing))
            MapDisplay(game.displayTiles, game.currentBackgroundColor)
            Spacer(Modifier.width(displaySpacing))
            when (game.inputMode) {
                InputMode.NORMAL -> {
                    SideHud(
                        game.playerDisplayStats
                            .plus(
                                listOf(
                                    LabeledTextDataBundle("Turn", game.turn.toString(), White),
                                    LabeledTextDataBundle("Camera", game.camera.coordinates.printed(), White),
                                    game.underCamera,
                                    game.underCameraHealth,
                                )
                            )
                    )
                }
                InputMode.INVENTORY -> {
                    SideHud(
                        game.inventoryLabels
                    )
                }
                InputMode.ABILITIES -> {
                    SideHud(
                        game.abilityLabels
                    )
                }
            }

        }
        Spacer(Modifier.height(displaySpacing))
        Row {
            BottomConsole(game.displayMessages)
        }
    }
}