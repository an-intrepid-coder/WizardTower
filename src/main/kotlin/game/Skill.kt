/**
 * Skill class and functions which deal with collections of skills.
 */

package game

enum class Attribute {
    STRENGTH,
    DEXTERITY,
    INTELLIGENCE,
    HEALTH
}

enum class SkillDifficultyLevel {
    EASY,
    AVERAGE,
    HARD,
    VERY_HARD
}

sealed class Skill(
    val name: String,
    val passive: Boolean, // Whether it is used in the abilities menu
    var level: Int = 0,
    val maxLevel: Int? = null,
    val controllingAttributes: List<Attribute>, // Sometimes there's more than 1.
    val difficultyLevel: SkillDifficultyLevel,
    // NOTE: Tech Levels not implemented yet
    val defaultAttributes: List<Attribute>, // Sometimes there's more than 1.
    val defaultLevel: Int,
) {
    fun costOfNextLevel(buyer: Character): Int {
        return 1 // TODO: Implement formula, taking the character as a parameter
    }

    // TODO: defaults

    class Shield : Skill(
        name = "Shield",
        passive = true,
        level = 0,
        maxLevel = null,
        controllingAttributes = listOf(Attribute.DEXTERITY),
        difficultyLevel = SkillDifficultyLevel.EASY,
        defaultAttributes = listOf(Attribute.DEXTERITY),
        defaultLevel = -4,
    )

    // TODO: More skills!
}