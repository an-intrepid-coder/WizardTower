package display

import androidx.compose.ui.graphics.Color
import game.Coordinates

/**
 * All the information needed to put a Cell on display.
 */
data class CellDisplayBundle(
    val displayValue: String,
    val displayColor: Color,
    val coordinates: Coordinates,
)