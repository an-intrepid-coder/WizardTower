package game

import androidx.compose.ui.graphics.Color
import display.*

// These are both limitations of the way I am handling input at the moment and I may change that in the future.
const val MAX_INVENTORY_SIZE = 26
const val MAX_ABILITIES = 26

/**
 * The Actor sealed class. Actors in the game will be instances of subclasses of Actor.
 */
sealed class Actor(
    // Name:
    val name: String,

    // Coordinates:
    var coordinates: Coordinates,

    // What will show up in Compose:
    val displayValue: String,
    val displayColor: Color,

    // Health (even "items" have it):
    var health: Int,
    var maxHealth: Int,

    // Ability Points (for spells and abilities):
    var abilityPoints: Int,
    var maxAbilityPoints: Int,

    // Faction, if any:
    var maybeFaction: Faction? = null,

    // Special boolean for the player:
    val isPlayer: Boolean = false,

    // Amount of gold:
    var gold: Int = 0,

    // Inventory, if any:
    var inventory: MutableList<Consumable>? = null,

    // Abilities (unlike inventory, this is never null):
    var abilities: MutableList<Ability> = mutableListOf()
) {
    /**
     * Removes an item from the Actor's inventory, if possible.
     */
    fun removeAbility(ability: Ability) {
        abilities.remove(ability)
    }

    /**
     * Adds an ability to the Actor's inventory, if possible.
     */
    fun addAbility(ability: Ability) {
        if (abilities.size < MAX_ABILITIES)
            abilities.add(ability)
    }

    /**
     * Returns true if the actor can see the given coordinates.
     */
    fun canSee(targetCoordinates: Coordinates, game: WizardTowerGame): Boolean {
        val sightLineClear = coordinates
            .bresenhamLineTo(targetCoordinates)
            .none { game.tilemap.getTileOrNull(it)!!.blocksSight }

        val targetInRange = targetCoordinates.chebyshevDistance(coordinates) <= defaultVisionRangeRadius

        return sightLineClear && targetInRange
    }

    /**
     * Sets the Actor's health to 0.
     */
    fun kill() {
        health = 0
    }

    /**
     * Returns true if the Actor has an inventory.
     */
    fun hasInventory(): Boolean {
        return inventory != null
    }

    /**
     * Removes an item from the Actor's inventory, if possible.
     */
    fun removeConsumable(item: Consumable) {
        if (hasInventory() && inventory!!.contains(item))
            inventory!!.remove(item)
    }

    /**
     * Adds an item to the Actor's inventory, if possible.
     */
    fun addConsumable(item: Consumable) {
        if (hasInventory() && inventory!!.size < MAX_INVENTORY_SIZE)
            inventory!!.add(item)
    }

    /**
     * Attempts to move an Actor one tile in the given Direction, on the given Tilemap. Returns true if
     * the movement was successful, and false otherwise.
     */
    fun move(
        direction: Direction,
        game: WizardTowerGame
    ): Boolean {
        val target = Coordinates(
            x = coordinates.x + direction.dx,
            y = coordinates.y + direction.dy
        )
        var moved = false
        game.tilemap
            .getTileOrNull(target)
            ?.let { tile ->
                // todo: Actor vs. Actor combat

                val maybeActor = game
                    .actors
                    .firstOrNull { it.coordinates.matches(tile.coordinates) }

                if (direction.matches(Direction.Stationary()) || (tile.isPassable && maybeActor == null)) {
                    coordinates = target
                    moved = true
                }
            }
        return moved
    }

    /**
     * Describes the Actor with a list of Messages.
     */
    fun describe(): List<Message> {
        val messages = mutableListOf<Message>()
        // todo: implement
        return messages
    }

    /**
     * Returns a CellDisplayBundle corresponding to the given Actor. Can be modified for overlays.
     */
    fun toCellDisplayBundle(
        overlayMode: OverlayType,
        decoupledCamera: Coordinates? = null,
    ): CellDisplayBundle {
        return if (overlayMode == OverlayType.FACTION)
            CellDisplayBundle(displayValue, factionColors[maybeFaction]!!, coordinates)
        else if (decoupledCamera != null && decoupledCamera.matches(coordinates))
            CellDisplayBundle(displayValue, BrightPurple, coordinates)
        else
            CellDisplayBundle(displayValue, displayColor, coordinates)
    }

    /**
     * Changes the Actor's health. Does not allow it to go below 0 or over maxHealth.
     */
    fun changeAbilityPoints(amount: Int) {
        abilityPoints = abilityPoints
            .plus(amount)
            .coerceAtMost(maxAbilityPoints)
            .coerceAtLeast(0)
    }

    /**
     * Changes the Actor's health. Does not allow it to go below 0 or over maxHealth.
     */
    fun changeHealth(amount: Int) {
        health = health
            .plus(amount)
            .coerceAtMost(maxHealth)
            .coerceAtLeast(0)
    }

    /**
     * Returns true if the Actor has greater than 0 health.
     */
    fun isAlive(): Boolean {
        return health > 0
    }

    /**
     * Changes the Actor's gold. Does not allow gold to go below 0.
     */
    fun changeGold(amount: Int) {
        gold = gold
            .plus(amount)
            .coerceAtLeast(0)
    }

    /**
     * A literal target for practice.
     */
    class Target(
        coordinates: Coordinates
    ) : Actor(
        name = "Practice Target",
        coordinates = coordinates,
        displayValue = "p",
        displayColor = White,
        maybeFaction = Faction.HOSTILE,
        health = 10,
        maxHealth = 10,
        abilityPoints = 0,
        maxAbilityPoints = 0,
    )

    /**
     * The Player Actor.
     */
    class Player(
        coordinates: Coordinates,
    ) : Actor(
        name = "Player",
        coordinates = coordinates,
        displayValue = "@",
        displayColor = White,
        isPlayer = true,
        maybeFaction = Faction.PLAYER,

        // Stats are wholly arbitrary for now. Will balance down the road:
        health = 10,
        maxHealth = 10,
        abilityPoints = 30,
        maxAbilityPoints = 30,

        inventory = mutableListOf(),
    ) {
        /**
         * Export the player's abilities as a list of (a..z)-labeled strings.
         */
        fun exportAbilityStrings(): List<LabeledTextDataBundle> {
            return abilities
                .zip(('a'..'z').toList())
                .map { pair ->
                    LabeledTextDataBundle("${pair.second}", pair.first.name, White)
                }
        }

        /**
         * Export the player's inventory as a list of (a..z)-labeled strings.
         */
        fun exportInventoryStrings(): List<LabeledTextDataBundle> {
            return inventory!!
                .zip(('a'..'z').toList())
                .map { pair ->
                    LabeledTextDataBundle("${pair.second}", pair.first.name, White)
                }
        }

        /**
         * Export the player's information to the GUI.
         */
        fun exportStatsToCompose(): List<LabeledTextDataBundle> {
            return listOf(
                LabeledTextDataBundle("Gold", gold.toString(), White),
                LabeledTextDataBundle("Health", "$health/$maxHealth", White),
                LabeledTextDataBundle("Ability Points", "$abilityPoints/$maxAbilityPoints", White)
            )
        }
    }
}