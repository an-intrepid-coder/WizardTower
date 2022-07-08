package game

/**
 * An "ability" can be a spell, the properties of a potion, a neat physical ability or anything else which can
 * be most-easily expressed in this format. The idea is to keep it flexible.
 */
sealed class Ability(
    val name: String,
    var effect: ((WizardTowerGame, Character, Character?) -> Unit)?, // format: (game, caster, target?)?
) {
    override fun toString(): String {
        return name
    }

    class AllOutDefense : Ability(
        name = "All-Out Defense",
        effect = { game, caster, _ ->
            caster.allOutDefense = true // TODO: Implement combat system
        }
    )

    class Parry : Ability(
        name = "Parry",
        effect = { game, caster, target ->
            caster.parrying = target // TODO: Implement combat system
        }
    )

    class Block : Ability(
        name = "Block",
        effect = { game, caster, target ->
            caster.blocking = target // TODO: Implement combat system
        }
    )
    // TODO: Synchronize the above 3 in the combat system, once implemented.

    class Swing : Ability(
        name = "Swing",
        effect = { game, caster, target ->
            // TODO: Implement combat system
        }
    )

    class Thrust : Ability(
        name = "Thrust",
        effect = { game, caster, target ->
            // TODO: Implement combat system
        }
    )
}