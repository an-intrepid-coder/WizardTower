package game

/**
 * Returns true with a frequency of chanceOf/outOf.
 */
fun withChance(outOf: Int, chanceOf: Int): Boolean {
    return (0..outOf).random() < chanceOf
}

fun coinFlip(): Boolean {
    return withChance(1, 1)
}