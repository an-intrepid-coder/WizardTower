package game

data class Bounds(
    val xRange: IntRange,
    val yRange: IntRange
) {
    fun inBounds(coordinates: Coordinates): Boolean {
        return coordinates.x in xRange && coordinates.y in yRange
    }

    fun withoutEdges(): Bounds {
        return Bounds(
            xRange = (xRange.first + 1) until xRange.last,
            yRange = (yRange.first + 1) until yRange.last
        )
    }
}
