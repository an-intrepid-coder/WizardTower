package game

import androidx.compose.ui.graphics.Color
import inputoutput.*

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
    var abilities: MutableList<Ability> = mutableListOf(),

    // Path: The current Coordinates path the Actor is following, if any:
    var path: MutableList<Coordinates>? = null,

    // Behavior, if any:
    var behavior: ((WizardTowerGame, Actor) -> Unit)? = null, // format: (Game, Self)
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
            .none { game.scene.tilemap.getTileOrNull(it)!!.blocksSight }

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
     *
     * todo: A more complex time/initiative system.
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
        game.scene.tilemap
            .getTileOrNull(target)
            ?.let { tile ->
                val maybeActor = game
                    .scene
                    .actors
                    .firstOrNull { it.coordinates == tile.coordinates }

                if (direction.matches(Direction.Stationary()) || (tile.isPassable && maybeActor == null)) {
                    coordinates = target
                    moved = true
                } else if (maybeActor != null && wouldFight(maybeActor)) {
                    val dmg = 1 // for now
                    maybeActor.changeHealth(-dmg)
                    game.messageLog.addMessage(
                        Message(
                            turn = game.turn,
                            text = "$name struck ${maybeActor.name} for $dmg damage.",
                            textColor = CautionYellow,
                        )
                    )
                    if (!maybeActor.isAlive())
                        game.messageLog.addMessage(
                            Message(
                                turn = game.turn,
                                text = "${maybeActor.name} dies!",
                                textColor = CautionYellow,
                            )
                        )

                    // Counts as movement for the sake of turn processing
                    moved = true
                }
            }
        return moved
    }

    /**
     * Compares two actors to determine if movement-collisions result in combat.
     *
     * Note: Eventually I will have a more fine-grained faction system than just Player/Neutral/Hostile.
     */
    private fun wouldFight(actor: Actor): Boolean {
        return (maybeFaction == Faction.PLAYER && actor.maybeFaction == Faction.HOSTILE)
                || (maybeFaction == Faction.HOSTILE && actor.maybeFaction == Faction.PLAYER)
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
        else if (decoupledCamera != null && decoupledCamera == coordinates)
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
     * Moves the Actor in a random direction.
     */
    fun moveRandom(game: WizardTowerGame) {
        val directionToMove = allDirections.random()
        move(directionToMove, game)
    }

    /**
     * Moves the Actor towards the first Tile in their saved path.
     */
    fun moveAlongPath(game: WizardTowerGame) {
        if (path == null)
            return
        else if (path!!.isEmpty())
            return

        val directionToMove = coordinates
            .relativeTo(path!!.first())

        move(directionToMove, game)
        path!!.removeFirst()
    }

    /**
     * Large, scaled quadrupeds with nasty dispositions.
     */
    class Barg(
        coordinates: Coordinates,
    ) : Actor(
        name = "Barg",
        coordinates = coordinates,
        displayValue = "B",
        displayColor = White,
        maybeFaction = Faction.HOSTILE,
        health = 10,
        maxHealth = 10,
        abilityPoints = 0,
        maxAbilityPoints = 0,
        path = mutableListOf(),
        behavior = { game, self ->
            /*
                This behavior lambda generates an A* path to the player and takes the first step along that path,
                if the actor can see the player. If the actor can't see the player, then it either moves along the
                path to the player's last-seen location or takes a move in a random direction.

                I may make Behavior its own class in the near future.
             */
            val player = game.getPlayer()
            when (self.canSee(player.coordinates, game)) {
                true -> {
                    self.path = AStarPath.Direct(
                        start = self.coordinates,
                        goal = player.coordinates,
                        game = game,
                        actor = self,
                    ).path?.toMutableList() ?: error("A* Path null result.")
                    self.path!!.removeFirst()

                    self.moveAlongPath(game)
                }
                else -> {
                    when (self.path!!.isEmpty()) {
                        true -> self.moveRandom(game)
                        else -> self.moveAlongPath(game)
                    }
                }
            }
        }
    )

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
        behavior = { game, self ->
            val fluffChanceOutOf100 = 1
            if (game.getPlayer().canSee(self.coordinates, game) && withChance(100, fluffChanceOutOf100))
                game.messageLog.addMessage(
                    Message(
                        turn = game.turn,
                        text = "The practice target exists.",
                        textColor = BrightBlue,
                    )
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

        // Stats are wholly arbitrary for now. Will balance down the road:
        health = 10,
        maxHealth = 10,
        abilityPoints = 30,
        maxAbilityPoints = 30,

        inventory = mutableListOf(),
    ) {
        /**
         * A generic function for returning a list of abilities, inventory items, etc. as a list of
         * alphabetically-keyed items in a list of LabeledTextDataBundle.
         */
        fun <T> exportAlphabetizedStrings(list: List<T>): List<LabeledTextDataBundle> {
            return list
                .zip(('a'..'z').toList())
                .map { pair ->
                    LabeledTextDataBundle("${pair.second}", pair.first.toString(), White)
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