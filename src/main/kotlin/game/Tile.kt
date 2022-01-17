package game

import androidx.compose.ui.graphics.Color
import inputoutput.BrightPurple
import inputoutput.CellDisplayBundle
import inputoutput.White
import inputoutput.randomFoggyColor

enum class TileType {
    FLOOR,
    WALL,
}

/**
 * The Tile class forms the backbone of the game's geography. It is a sealed class, and each kind of Tile is a sub-class
 * of it.
 */
sealed class Tile(
    val coordinates: Coordinates,
    val displayValue: String,
    val displayColor: Color,
    val tileType: TileType,
    var isPassable: Boolean = true,
    var explored: Boolean = false,
    var visibleToPlayer: Boolean = false,
    var blocksSight: Boolean = false
) {
    /**
     * Marks the tile as being seen by the player.
     */
    fun seen() {
        visibleToPlayer = true
        explored = true
    }

    /**
     * Describes the Tile with a list of Messages.
     */
    fun describe(): List<Message> {
        val messages = mutableListOf<Message>()
        // todo
        return messages
    }

    /**
     * Returns either the natural displayColor of the Tile or randomFoggyColor().
     */
    private fun cellColor(): Color {
        return if (visibleToPlayer)
            displayColor
        else
            randomFoggyColor()
    }

    /**
     * Returns a representation of the Tile for use in the GUI.
     */
    fun toCellDisplayBundle(): CellDisplayBundle {
        return CellDisplayBundle(
                displayValue = displayValue,
                displayColor = cellColor(),
                coordinates = coordinates
            )
    }

    /**
     * Returns a representation of the Tile altered for the targeting interface.
     */
    fun targetedCell(
        displayValue: String,
    ): CellDisplayBundle {
        return CellDisplayBundle(
            displayValue = displayValue,
            displayColor = BrightPurple,
            coordinates = coordinates,
        )
    }

    /**
     * A prototypical floor Tile. Can be walked on and does not block line of sight.
     */
    class Floor(
        coordinates: Coordinates
    ) : Tile(
        coordinates = coordinates,
        displayValue = ".",
        displayColor = White,
        tileType = TileType.FLOOR
    )

    /**
     * A prototypical wall tile. Can't be walked on and does block sight.
     */
    class Wall(
        coordinates: Coordinates,
    ) : Tile(
        coordinates = coordinates,
        displayValue = "#",
        displayColor = White,
        tileType = TileType.WALL,
        isPassable = false,
        blocksSight = true,
    )
}