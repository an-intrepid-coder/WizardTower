package game

// A list of all potential movement directions:
val allDirections = listOf(
    Direction.Up(),
    Direction.UpRight(),
    Direction.Right(),
    Direction.DownRight(),
    Direction.Down(),
    Direction.DownLeft(),
    Direction.Left(),
    Direction.UpLeft(),
    Direction.Stationary()
)

/**
 * Direction contains a dx and a dy which are equal to the change in Coordinates required to perform the movement.
 */
sealed class Direction(
    val dx: Int,
    val dy: Int,
) {
    override fun equals(other: Any?): Boolean {
        other as Direction
        return this.dx == other.dx && this.dy == other.dy
    }

    override fun hashCode(): Int {
        return 31 * dx + dy
    }

    class Raw(dx: Int, dy: Int) : Direction(dx, dy)

    class Up : Direction(0, -1)

    class UpRight : Direction(1, -1)

    class Right : Direction(1, 0)

    class DownRight : Direction(1, 1)

    class Down : Direction(0, 1)

    class DownLeft : Direction(-1, 1)

    class Left : Direction(-1, 0)

    class UpLeft : Direction(-1, -1)

    class Stationary : Direction(0, 0)
}