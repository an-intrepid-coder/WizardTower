package game

enum class DamageType { // TODO: Implement various damage types in combat system
    BURNING,            //       For now, all damage is non-typed.
    CRUSHING,
    CUTTING,
    IMPALING,
    SMALL_PIERCING,
    PIERCING,
    LARGE_PIERCING
}

/**
 * Items which can be armor, weapons, or anything else.
 */
class InventoryItem(
    val name: String,
    val damageResistance: Int,
    val armor: Boolean,
    // TODO: Weapon stuff. Most attacks will happen via the weapon's ability/effect through the menu. <-- BOOKMARK
    val twoHanded: Boolean,
    val useEffect: ((WizardTowerGame, Actor, Actor?) -> Unit)?,  // game, user, target (if any)
    // TODO: Charges and other specifics of useEffect()
) {
    var equipped: Boolean = false


    /**
     * Describes the InventoryItem with a list of Messages.
     */
    fun describe(): List<Message> {
        val messages = mutableListOf<Message>()
        // todo: implement
        return messages
    }

    override fun toString(): String {
        return name
    }
}