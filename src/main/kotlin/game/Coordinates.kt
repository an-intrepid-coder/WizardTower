package game

import kotlin.math.abs
import kotlin.math.max

/**
 * Simple Coordinates class to use instead of Pair<Int, Int>.
 */
class Coordinates(
    val x: Int,
    val y: Int,
) {
    /**
     * Returns true if the Coordinates match the other Coordinates.
     */
    fun matches(other: Coordinates): Boolean {
        return x == other.x && y == other.y
    }

    /**
     * Returns the Direction one would need to move to go from this Coordinate to the given one.
     */
    fun relativeTo(other: Coordinates): Direction {
        val dx = other.x - x
        val dy = other.y - y
        return Direction.Raw(dx, dy)
    }

    /**
     * Returns a neighboring Coordinate in the given direction.
     */
    fun moved(direction: Direction): Coordinates {
        return Coordinates(
            x = x + direction.dx,
            y = y + direction.dy
        )
    }

    /**
     * Prints the Coordinates.
     */
    fun printed(): String {
        return "($x, $y)"
    }

    /**
     * Returns distance from one Coordinates to another.
     */
    fun chebyshevDistance(other: Coordinates): Int {
        return max(abs(other.x - x), abs(other.y - y))
    }

    /**
     * Returns all neighbor coordinates that exist within the given bounds.
     */
    fun neighbors(bounds: Bounds): List<Coordinates> {
        return allDirections
            .asSequence()
            .filter { !it.matches(Direction.Stationary()) }
            .map { Coordinates(this.x + it.dx, this.y + it.dy) }
            .filter { bounds.inBounds(it) }
            .toList()
    }

    /**
     * Returns a Bresenham line between this coordinate and another as List<Coordinates>.
     */
    fun bresenhamLineTo(other: Coordinates): List<Coordinates> {
        /*
            Algorithm pseudocode courtesy of Wikipedia:
            https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm#All_cases
         */
        val line = mutableListOf<Coordinates>()
        var plottingX = x
        var plottingY = y
        val dx = abs(other.x - x)
        val dy = -abs(other.y - y)
        val sx = if (x < other.x) 1 else -1
        val sy = if (y < other.y) 1 else -1
        var err = dx + dy
        while (true) {
            if (plottingX == other.x && plottingY == other.y) break
            line.add(Coordinates(plottingX, plottingY))
            val err2 = err * 2
            if (err2 >= dy) {
                err += dy
                plottingX += sx
            }
            if (err2 <= dx) {
                err += dx
                plottingY += sy
            }
        }
        return line
    }
}