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
            when (game.inputMode.value) {
                InputMode.NORMAL -> {
                    SideHud(
                        game.playerDisplayStats
                    )
                    Spacer(Modifier.width(displaySpacing))
                    SideHud(
                        listOf(
                            LabeledTextDataBundle("Input Mode", game.inputMode.value.toString(), White),
                            LabeledTextDataBundle("Sync Indicator", game.syncIndicator.value, White),
                            LabeledTextDataBundle("Turn", game.turn.toString(), White),
                            LabeledTextDataBundle("Camera", game.scene.camera.coordinates.toString(), White),
                            game.underCamera,
                            game.underCameraHealth,
                        )
                    )
                }
                InputMode.INVENTORY -> {
                    SideHud(
                        listOf(LabeledTextDataBundle("Input Mode", game.inputMode.value.toString(), White)).plus(
                            game.inventoryLabels
                        )
                    )
                }
                InputMode.ABILITIES -> {
                    SideHud(
                        listOf(LabeledTextDataBundle("Input Mode", game.inputMode.value.toString(), White)).plus(
                            game.abilityLabels
                        )
                    )
                }
                InputMode.BIND_KEY -> {
                    SideHud(
                        listOf(
                            LabeledTextDataBundle("Input Mode", game.inputMode.value.toString(), White),
                            LabeledTextDataBundle(
                                label = "Binding",
                                value = game.maybeRebindingKey.toString(),
                                color = White,
                            )
                        )
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