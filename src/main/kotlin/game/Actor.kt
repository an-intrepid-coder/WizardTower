package game

import androidx.compose.ui.graphics.Color
import display.*

const val MAX_INVENTORY_SIZE = 26

/**
 * The Actor sealed class. Actors in the game will be instances of subclasses of Actor.
 */
sealed class Actor(
    val name: String,
    var coordinates: Coordinates,
    val displayValue: String,
    val displayColor: Color,
    var health: Int,
    var maxHealth: Int,
    var maybeFaction: Faction? = null,
    val isPlayer: Boolean = false,
    var gold: Int = 0,
    var inventory: MutableList<Actor>? = null,
    var itemEffect: ((WizardTowerGame, Actor, Actor) -> Unit)? = null,
) {
    /**
     * Sets the Actor's health to 0.
     */
    fun kill() {
        health = 0
    }

    /**
     * Returns true if the Actor has an itemEffect.
     */
    fun hasItemEffect(): Boolean {
        return itemEffect != null
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
    fun removeItem(actor: Actor) {
        if (hasInventory() && inventory!!.contains(actor))
            inventory!!.remove(actor)
    }

    /**
     * Adds an item to the Actor's inventory, if possible.
     */
    fun addItem(actor: Actor) {
        if (hasInventory() && inventory!!.size < MAX_INVENTORY_SIZE)
            inventory!!.add(actor)
    }

    /**
     * Attempts to move an Actor one tile in the given Direction, on the given Tilemap. Returns true if
     * the movement was successful, and false otherwise.
     */
    fun move(
        direction: Direction,
        tilemap: Tilemap,
    ): Boolean {
        val target = Coordinates(
            x = coordinates.x + direction.dx,
            y = coordinates.y + direction.dy
        )
        var moved = false
        tilemap
            .getTileOrNull(target)
            ?.let { tile ->
                // todo: Actor-vs-Actor collision detection
                if (tile.isPassable) {
                    coordinates = target
                    moved = true
                }
            }
        return moved
    }

    /**
     * Describes the actor with a list of Messages.
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
     * Very simple health potion.
     */
    class MinorHealingPotion(
        coordinates: Coordinates
    ) : Actor(
        name = "Minor Healing Potion",
        coordinates = coordinates,
        displayValue = "!",
        displayColor = BrightBlue,
        health = 3,
        maxHealth = 3,
        itemEffect = { game, self, triggerer ->
            val healAmount = 10
            triggerer.changeHealth(healAmount)
            self.kill()
            game.messageLog.addMessage(
                Message(game.turn, "${triggerer.name} healed $healAmount health using a ${self.name}", GoGreen)
            )
        }
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
        health = 10,
        maxHealth = 10,
        inventory = mutableListOf(
            MinorHealingPotion(coordinates),
            MinorHealingPotion(coordinates),
            MinorHealingPotion(coordinates),
        ),
    ) {
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
            )
        }
    }
}