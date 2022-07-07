package game

import androidx.compose.ui.graphics.Color
import inputoutput.*
import kotlin.math.floor

// These are both limitations of the way I am handling input at the moment, and I may change that in the future.
const val MAX_INVENTORY_SIZE = 26
const val MAX_ABILITIES = 26

/**
 * The Actor sealed class. Actors in the game will be instances of subclasses of Actor.
 */
sealed class Character(
    // Name:
    val name: String,

    // Coordinates:
    var coordinates: Coordinates,

    // What will show up in Compose:
    val displayValue: String,
    val displayColor: Color,

    var strength: Int,
    var dexterity: Int,
    var intelligence: Int,
    var health: Int,

    // Special boolean for the player:
    val isPlayer: Boolean = false,

    // Amount of wealth (gold, etc.):
    var wealth: Int = 0,

    // Inventory, if any:
    var inventory: MutableList<InventoryItem> = mutableListOf(),

    // Abilities (unlike inventory, this is never null):
    var abilities: MutableList<Ability> = mutableListOf(),

    // Skills which may or may not map to active abilities:
    var skills: MutableList<Skill> = mutableListOf(),

    // Path: The current Coordinates path the Actor is following, if any:
    var path: MutableList<Coordinates>? = null,

    // Behavior, if any:
    var behavior: ((WizardTowerGame, Character) -> Unit)? = null, // format: (Game, Self)
) {
    var hitPoints: Int = strength
    var maxHitPoints: Int = strength

    // If the actor is using all-out-defense:
    var allOutDefense: Boolean = false // TODO: Implement in combat system
    var parrying: Character? = null // TODO: Implement in combat system
    var blocking: Character? = null // TODO: Implement in combat system

    fun getBlocking(): String {
        return if (blocking == null) {
            "None"
        } else {
            blocking!!.name
        }
    }

    fun getParrying(): String {
        return if (parrying == null) {
            "None"
        } else {
            parrying!!.name
        }
    }

    fun changeHitPoints(amount: Int) {
        hitPoints += amount
            .coerceAtLeast(0)
            .coerceAtMost(maxHitPoints)
    }

    var fatiguePoints = health
    var maxFatiguePoints = health

    fun changeFatiguePoints(amount: Int) {
        fatiguePoints += amount
            .coerceAtLeast(0)
            .coerceAtMost(maxFatiguePoints)
    }

    fun getWill(): Int {
        return intelligence
    }

    fun getPerception(): Int {
        return intelligence
    }

    fun getBasicLift(): Double {
        val raw = (strength * strength).toDouble() / 5
        return if (raw > 10) {
            kotlin.math.round(raw)
        } else {
            raw
        }
    }

    fun getBasicSpeed(): Double {
        return (health + dexterity).toDouble() / 4
    }

    fun getBasicMove(): Int {
        return floor(getBasicSpeed()).toInt()
    }

    fun getDamageResistance(): Int {
        return inventory.sumOf { it.damageResistance }
    }

    fun getSkillOrNull(skillName: String): Skill? {
        return skills.find { it.name == skillName }
    }

    fun getDodge(): Int {
        // TODO: Encumbrance to fully implement modifiers
        return floor(getBasicSpeed()).toInt() + 3
    }

    fun getBlock(): Int {
        val shieldSkill = getSkillOrNull("Shield")
        return if (shieldSkill == null) {
            // TODO: ^ Default skills
            3
        } else {
            3 + floor(shieldSkill.level / 2.0).toInt()
        }
    }

    fun getParry(): Int {
        // TODO: Unarmed version
        return 3 // TODO: Implement
    }

    /**
     * Adds an ability to the Actor's abilities list, if possible.
     */
    fun addAbility(ability: Ability) {
        if (abilities.size < MAX_ABILITIES)
            abilities.add(ability)
    }

    /**
     * Adds a Skill to the Character's skills list.
     */
    fun addSkill(skill: Skill) {
        skills.add(skill)
    }

    /**
     * Adds an item to the Actor's inventory, if possible.
     */
    fun addInventoryItem(item: InventoryItem) {
        if (inventory.size < MAX_INVENTORY_SIZE)
            inventory.add(item)
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
     * Changes the Actor's wealth.
     */
    fun changeWealth(amount: Int) { wealth = wealth.plus(amount) }

    /**
     * Describes the Actor with a list of Messages.
     */
    fun describe(): List<Message> {
        val messages = mutableListOf<Message>()
        // todo: implement
        return messages
    }

    /**
     * Returns true if the Actor has greater than 0 health.
     */
    fun isAlive(): Boolean {
        return health > 0
    }

    /**
     * Sets the Actor's health to 0.
     * // TODO: Implement GURPS negative HP rules.
     */
    fun kill() {
        hitPoints = 0
    }

    /**
     * Attempts to move an Actor one tile in the given Direction, on the given Tilemap. Returns true if
     * the movement was successful, and false otherwise.
     *
     * todo: A more complex time/initiative system.
     * todo: Additional cost for diagonal movement.
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
                    .characters
                    .firstOrNull { it.coordinates == tile.coordinates }

                if (direction == Direction.Stationary() || (tile.isPassable && maybeActor == null)) {
                    coordinates = target
                    moved = true
                } else {
                    game.messageLog.addMessage(
                        Message(
                            turn = game.turn,
                            text = "$name can't move there!",
                            textColor = CautionYellow,
                        )
                    )
                    game.syncGui()
                }
            }
        return moved
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
     * Moves the Actor in a random direction.
     */
    fun moveRandom(game: WizardTowerGame) {
        val directionToMove = allDirections.random()
        move(directionToMove, game)
    }

    /**
     * Removes an ability from the Actor's abilities, if possible.
     */
    fun removeAbility(ability: Ability) {
        abilities.remove(ability)
    }

    /**
     * Removes an item from the Actor's inventory, if possible.
     */
    fun removeConsumable(item: InventoryItem) {
        if (inventory.contains(item))
            inventory.remove(item)
    }

    /**
     * Returns a CellDisplayBundle corresponding to the given Actor. Can be modified for overlays.
     */
    fun toCellDisplayBundle(
        overlayMode: OverlayType,
        decoupledCamera: Coordinates? = null,
    ): CellDisplayBundle {
        return if (overlayMode == OverlayType.FACTION)
            // TODO: Rework factions to be relative reaction modifiers or something
            CellDisplayBundle(displayValue, factionColors[Faction.NEUTRAL]!!, coordinates)
        else if (decoupledCamera != null && decoupledCamera == coordinates)
            CellDisplayBundle(displayValue, BrightPurple, coordinates)
        else
            CellDisplayBundle(displayValue, displayColor, coordinates)
    }

    /**
     * Large, scaled quadrupeds with nasty dispositions.
     */
    class Barg( // TODO: Revise this enemy for the new stats system
        coordinates: Coordinates,
    ) : Character(
        name = "Barg",
        coordinates = coordinates,
        displayValue = "B",
        displayColor = White,
        // Placeholder stats:
        strength = 10,
        dexterity = 10,
        intelligence = 10,
        health = 10,
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
                        character = self,
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
     * The Player Actor.
     */
    class Player(
        coordinates: Coordinates,
    ) : Character(
        name = "Player",
        coordinates = coordinates,
        displayValue = "@",
        displayColor = White,
        isPlayer = true,

        // Stats are wholly arbitrary for now. Will balance down the road:
        strength = 10,
        dexterity = 10,
        intelligence = 10,
        health = 10,

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
                LabeledTextDataBundle("Gold", wealth.toString(), White),
                LabeledTextDataBundle("HP", "$hitPoints/$maxHitPoints", White),
                LabeledTextDataBundle("DR", "${getDamageResistance()}", White),
                LabeledTextDataBundle("ST", "$strength", White),
                LabeledTextDataBundle("DX", "$dexterity", White),
                LabeledTextDataBundle("IQ", "$intelligence", White),
                LabeledTextDataBundle("HT", "$health", White),
                LabeledTextDataBundle("FP", "$fatiguePoints/$maxFatiguePoints", White),
                LabeledTextDataBundle("Will", "${getWill()}", White),
                LabeledTextDataBundle("Perception", "${getPerception()}", White),
                LabeledTextDataBundle("Basic Lift", "${getBasicLift()}", White),
                LabeledTextDataBundle("Basic Speed", "${getBasicSpeed()}", White),
                LabeledTextDataBundle("Basic Move", "${getBasicMove()}", White),
                LabeledTextDataBundle("Dodge", "${getDodge()}", White),
                LabeledTextDataBundle("Block", "${getBlock()}", White),
                LabeledTextDataBundle("Parry", "${getParry()}", White),
            )
        }
    }

    /**
     * A literal target for practice.
     */
    class Target(
        coordinates: Coordinates
    ) : Character(
        name = "Practice Target",
        coordinates = coordinates,
        displayValue = "p",
        displayColor = White,
        strength = 10,
        dexterity = 10,
        intelligence = 10,
        health = 10,
        behavior = { game, self ->
            if (game.getPlayer().canSee(self.coordinates, game) && rollD6().sum() == 18)
                game.messageLog.addMessage(
                    Message(
                        turn = game.turn,
                        text = "The practice target exists.",
                        textColor = BrightBlue,
                    )
                )
        }
    )
}