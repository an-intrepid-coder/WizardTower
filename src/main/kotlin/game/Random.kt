package game

/**
 * Rolls the desired number of dice with desired number of sides.
 * Returns a list with each die's result.
 */
fun rollDice(numSides: Int, numDice: Int): List<Int> {
    var result: List<Int> = listOf()
    for (i in (1..numDice)) {
        result = result.plus((0..numSides).random())
    }
    return result
}

/**
 * Roll desired number of d6. Defaults to 3. Returns a list
 * of the results.
 */
fun rollD6(numDice: Int = 3): List<Int> {
    return rollDice(6, numDice)
}

/**
 * 50/50 result; like a coin flip.
 */
fun coinFlip(): Boolean {
    return (0..1).random() == 1
}