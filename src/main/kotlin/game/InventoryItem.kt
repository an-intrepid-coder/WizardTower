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
sealed class InventoryItem(
    val name: String,
    val weight: Int,
    val cost: Int,
    val damageResistance: Int,
    val armor: Boolean,
    val shield: Boolean,
    val defenseBonus: Int,
    val weapon: Boolean,
    val twoHanded: Boolean,
    val useEffect: ((WizardTowerGame, Character, Character?) -> Unit)?,  // game, user, target (if any)
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

    // NOTE: Each individual class of item will be extensible with proc-gen, so that they are more like
    //       template classes. TODO: Implement procedural versions.

    class PaddedArmor : InventoryItem(
        name = "Padded Armor",
        weight = 12,
        cost = 150,
        damageResistance = 1,
        armor = true,
        shield = false,
        defenseBonus = 0,
        weapon = false,
        twoHanded = false,
        useEffect = null,
    )

    class SmallShield : InventoryItem(
        name = "Small Shield",
        weight = 8,
        cost = 40,
        damageResistance = 0,
        armor = false,
        shield = true,
        defenseBonus = 1,
        weapon = false,
        twoHanded = false,
        useEffect = null,
    )

    class MediumShield : InventoryItem(
        name = "Medium Shield",
        weight = 15,
        cost = 60,
        damageResistance = 0,
        armor = false,
        shield = true,
        defenseBonus = 2,
        weapon = false,
        twoHanded = false,
        useEffect = null,
    )

    class LargeShield : InventoryItem(
        name = "Large Shield",
        weight = 25,
        cost = 90,
        damageResistance = 0,
        armor = false,
        shield = true,
        defenseBonus = 3,
        weapon = false,
        twoHanded = false,
        useEffect = null,
    )

    // TODO: More individual items as their own classes, with room for some proc-gen.
}