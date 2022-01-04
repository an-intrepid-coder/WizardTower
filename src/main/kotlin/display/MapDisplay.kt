package display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

// The dimensions of the Map Display.
const val mapDisplayWidthNormal = 20
const val mapDisplayHeightNormal = 20

/**
 * Takes a list of display tiles and places them on the screen. Relies on a Tilemap to export the display tiles.
 * Currently, all tiles will have the same background color.
 */
@Composable
fun MapDisplay(
    displayTiles: List<List<CellDisplayBundle>>,
    backgroundColor: Color,
) {
    LazyColumn(
        modifier = Modifier
            .background(backgroundColor)
    ) {
        displayTiles.forEach { row ->
            item {
                Row {
                    row.forEach { cell ->
                        Text(cell.displayValue, color = cell.displayColor, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}