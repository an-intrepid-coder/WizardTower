package game

/**
 * An "ability" can be a spell, the properties of a potion, a neat physical ability or anything else which can
 * be most-easily expressed in this format. The idea is to keep it flexible.
 */
sealed class Ability(
    val name: String,
    var effect: ((WizardTowerGame, Actor, Actor?) -> Unit)?, // format: (game, caster, target?)?
) {
    override fun toString(): String {
        return name
    }

    class AllOutDefense : Ability(
        name = "All-Out Defense",
        effect = { game, caster, _ ->
            caster.allOutDefense = true // NOTE: Not implemented yet
        }
    )
}