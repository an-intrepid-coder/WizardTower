package game

/**
 * Items that have some effect on the game world before disappearing after a number of uses.
 * Each uses a single ability (for now). Instead of having subclasses, this is a lighter-weight
 * class which is meant to be generated on-the-fly. Will allow for more combinations later.
 */
class Consumable(
    val name: String,
    val ability: Ability,
    var charges: Int,
    var maxCharges: Int,
    var containedWithin: MutableList<Consumable>,
) {

    /**
     * Describes the Consumable with a list of Messages.
     */
    fun describe(): List<Message> {
        val messages = mutableListOf<Message>()
        // todo: implement
        return messages
    }

    /**
     * Returns true if the Consumable has any charges left.
     */
    fun hasCharges(): Boolean {
        return charges > 0
    }

    /**
     * Changes the Actor's health. Does not allow it to go below 0 or over maxHealth.
     */
    fun changeCharges(amount: Int) {
        charges = charges
            .plus(amount)
            .coerceAtMost(maxCharges)
            .coerceAtLeast(0)
    }

    /**
     * Invokes the Consumable's Ability.
     */
    fun use(game: WizardTowerGame, user: Actor, target: Actor?) {
        if (hasCharges()) {
            // Cast item's effect:
            ability.effect(game, user, target)

            // Decrease charges by 1:
            changeCharges(-1)

            // Remove from containing inventory if used the last charge:
            if (!hasCharges())
                containedWithin.remove(this)
        }
    }
}