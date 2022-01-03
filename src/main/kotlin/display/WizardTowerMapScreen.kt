package display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import game.WizardTowerGame

/**
 * The Map Screen is simply the MapDisplay enlarged to take the whole screen. While this Composable is active,
 * only a limited set of controls are available (movement controls, mainly).
 */
@Composable
fun WizardTowerMapScreen(
    game: WizardTowerGame,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGray),
        horizontalAlignment = Alignment.Start,
    ) {
        MapDisplay(game.displayTiles, game.currentBackgroundColor)
    }
}