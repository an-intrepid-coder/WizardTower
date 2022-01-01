package game

import androidx.compose.ui.graphics.Color
import display.*

// The default size of the scrollable maps (they can be larger than the display width):
const val defaultMapWidth = 100
const val defaultMapHeight = 100

const val visionRangeRadius = 8

/**
 * The Tilemap class is a wrapper over a 2D List of Tile objects with many functions for manipulating and generating
 * maps. For now, the whole map must have the same background color.
 */
sealed class Tilemap(
    val width: Int,
    val height: Int,
    var backgroundColor: Color,
) {
    protected var tiles: MutableList<MutableList<Tile>> = mutableListOf()

    /**
     * Returns a 1D list of all Tiles.
     */
    fun flattened(): List<Tile> {
        return tiles.flatten()
    }

    /**
     * Returns all Tiles of the given type.
     */
    fun allTilesOfType(tileType: TileType): List<Tile> {
        return tiles
            .flatten()
            .filter { it.tileType == tileType }
    }

    /**
     * Calculates the Field of View of the player. For now, it does not take the FOV of other Actors in to account.
     */
    fun calculateFieldOfView(player: Actor) {
        // Set all tiles to explored if they were visible:
        tiles.forEach { row ->
            row.forEach { tile ->
                if (tile.visibleToPlayer) {
                    tile.explored = true
                    tile.visibleToPlayer = false
                }
            }
        }

        // Determine the new set of visible tiles based on the player's Field of View:
        tilesInRadius(player.coordinates, visionRangeRadius).forEach { tile ->
            val sightLine = player.coordinates.bresenhamLine(tile.coordinates)
            if (sightLine.none { getTileOrNull(it)!!.blocksSight })
                tile.seen()
        }
    }

    /**
     * Returns all Tiles within a given radius around a given origin.
     */
    private fun tilesInRadius(
        origin: Coordinates,
        inRadius: Int
    ): List<Tile> {
        return tiles.asSequence()
            .flatten()
            .filter { origin.chebyshevDistance(it.coordinates) <= inRadius }
            .toList()
    }

    /**
     * Returns the Tile at the given coordinates, or null if it can't be found.
     */
    fun getTileOrNull(
        coordinates: Coordinates,
    ): Tile? {
        return tiles
            .flatten()
            .firstOrNull { it.coordinates.matches(coordinates) }
    }

    /**
     * Returns a random Tile of the given type.
     */
    fun randomTileOfType(tileType: TileType): Tile {
        return tiles
            .flatten()
            .filter { it.tileType == tileType }
            .random()
    }

    /**
     * Returns a random DeepSpace tile.
     */
    fun randomPassableTile(): Tile {
        return tiles
            .flatten()
            .filter { it.isPassable }
            .random()
    }

    /**
     * Using cameraPoint as the center Coordinates, returns a slice of the Tilemap which fits in the Map Display.
     */
    fun exportTilesToCompose(
        cameraPoint: Coordinates
    ): List<List<CellDisplayBundle>> {
        val tiles = mutableListOf<MutableList<CellDisplayBundle>>()

        val topLeft = Coordinates(
            x = cameraPoint.x - mapDisplayWidth / 2,
            y = cameraPoint.y - mapDisplayHeight / 2
        )

        repeat (mapDisplayHeight) { row ->
            val newRow = mutableListOf<CellDisplayBundle>()
            repeat (mapDisplayWidth) { col ->
                val target = Coordinates(
                    x = topLeft.x + col,
                    y = topLeft.y + row,
                )
                val tile = getTileOrNull(target)
                if (tile == null)
                    newRow.add(CellDisplayBundle(" ", Black, target))
                else
                    newRow.add(tile.toCellDisplayBundle())
            }
            tiles.add(newRow)
        }

        return tiles
    }

    /**
     * Returns true is the given Coordinates are an edge point.
     */
    private fun isEdgePoint(coordinates: Coordinates): Boolean {
        val x = coordinates.x
        val y = coordinates.y
        return x == 0 || y == 0 || x == width - 1 || y == height - 1
    }

    /**
     * Returns a blank map that is all Floor tiles, except for an outer edge of Wall tiles.
     */
    fun floorWithEdgeWalls(): MutableList<MutableList<Tile>> {
        val newMap = mutableListOf<MutableList<Tile>>()
        repeat (height) { row ->
            val newRow = mutableListOf<Tile>()
            repeat (width) { col ->
                val coordinates = Coordinates(col, row)
                if (isEdgePoint(coordinates))
                    newRow.add(Tile.Wall(coordinates))
                else
                    newRow.add(Tile.Floor(coordinates))
            }
            newMap.add(newRow)
        }
        return newMap
    }

    /**
     * A testing map which is a simple blank arena of Floor tiles, surrounded by wall tiles, with a few
     * Wall tiles scattered around on their own as pillars.
     */
    class DebugArena(
        width: Int = defaultMapWidth,
        height: Int = defaultMapHeight
    ) : Tilemap(width, height, Black) {
        init {
            tiles = floorWithEdgeWalls()

            val numPillars = 10
            repeat (numPillars) {
                val target = randomTileOfType(TileType.FLOOR).coordinates
                tiles[target.y][target.x] = Tile.Wall(target)
            }

            // Some items and stuff to come
        }
    }
}