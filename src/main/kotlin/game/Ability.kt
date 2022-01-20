package game

import inputoutput.BrightPurple
import inputoutput.CautionYellow
import inputoutput.GoGreen

enum class AbilityComponentRequirement {
    VERBAL, MANIPULAR
}

/**
 * An "ability" can be a spell, the properties of a potion, a neat physical ability or anything else which can
 * be most-easily expressed in this format. The idea is to keep it flexible.
 */
sealed class Ability(
    val name: String,
    val componentRequirements: Set<AbilityComponentRequirement>,
    var effect: ((WizardTowerGame, Actor, Actor?) -> Unit), // format: (game, caster, target?)
) {
    override fun toString(): String {
        return name
    }

    /**
     * Returns true if the caster is able to access all the required components for the ability.
     */
    fun canCast(caster: Actor): Boolean {
        return caster
            .componentFlags
            .none { it.key in componentRequirements && !it.value }
    }

    /**
     * Magic missile shoots a simple projectile at a target in line-of-sight.
     */
    class MagicMissile : Ability(
        name = "Magic Missile",
        componentRequirements = setOf(AbilityComponentRequirement.VERBAL, AbilityComponentRequirement.MANIPULAR),
        effect = { game, caster, target ->
            // Spell Stats (these are tentative):
            val magicMissileBaseDamageRange = 5..10
            val magicMissileBaseAbilityPointCost = 1

            /*
                Eventually the player will have access to means of increasing their FOV or spotting creatures
                outside it, which would allow this to fire at anything not blocked by a wall or something.
             */
            val magicMissileBaseRange = Int.MAX_VALUE / 2

            // It can be used on empty Tiles but wastes a turn:
            if (target == null) {
                game.messageLog.addMessage(
                    Message(
                        turn = game.turn,
                        text = "You shoot at nothing! What are you trying to shoot at?",
                        textColor = BrightPurple,
                    )
                )
            }

            // If the target is in range and in sight then the spell fires:
            else if (caster.canSee(target.coordinates, game) &&
                caster.coordinates.chebyshevDistance(target.coordinates) <= magicMissileBaseRange) {

                val damageDealt = magicMissileBaseDamageRange.random()
                target.changeHealth(-damageDealt)
                caster.changeAbilityPoints(-magicMissileBaseAbilityPointCost)
                game.messageLog.addMessage(
                    Message(
                        turn = game.turn,
                        text = "${caster.name} shoots a magical bolt at ${target.name}!",
                        textColor = CautionYellow,
                    )
                )
                game.messageLog.addMessage(
                    Message(
                        turn = game.turn,
                        text = "It does $damageDealt damage.",
                        textColor = CautionYellow,
                    )
                )
                if (!target.isAlive())
                    game.messageLog.addMessage(
                        Message(
                            turn = game.turn,
                            text = "${target.name} was slain!",
                            textColor = CautionYellow,
                        )
                    )
            }
        }
    )

    /**
     * Minor healing spell. Only targets self no matter what is selected.
     */
    class MinorHealSelf : Ability (
        name = "Minor Healing",
        componentRequirements = setOf(AbilityComponentRequirement.MANIPULAR),
        effect = { game, caster, _ ->
            val healAmount = 10
            caster.changeHealth(healAmount)
            game.messageLog.addMessage(
                Message(
                    turn = game.turn,
                    text = "${caster.name} magically heals $healAmount health!",
                    textColor = GoGreen
                )
            )
        }
    )
}