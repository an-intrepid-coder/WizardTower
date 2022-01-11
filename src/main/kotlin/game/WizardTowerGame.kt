package game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import display.*

enum class OverlayType {
    NONE,
    FACTION,
    PASSABLE,
    // more to come -- especially for environmental effects and complex spell interactions, eventually
}

enum class InputMode {
    NORMAL,
    INVENTORY,
    ABILITIES,
}

fun targetedTile(
    coordinates: Coordinates,
    displayValue: String,
): CellDisplayBundle {
    return CellDisplayBundle(
        displayValue = displayValue,
        displayColor = BrightPurple,
        coordinates = coordinates,
    )
}

val defaultUnderCameraLabel = LabeledTextDataBundle("Under Camera", "N/A", White)
val defaultUnderCameraHealthLabel = LabeledTextDataBundle("Health", "N/A", White)

@OptIn(ExperimentalComposeUiApi::class)
val movementKeyDirectionMap = mapOf(
    Key.H to Direction.Left(),
    Key.NumPad4 to Direction.Left(),
    Key.L to Direction.Right(),
    Key.NumPad6 to Direction.Right(),
    Key.J to Direction.Down(),
    Key.NumPad2 to Direction.Down(),
    Key.K to Direction.Up(),
    Key.NumPad8 to Direction.Up(),
    Key.N to Direction.DownRight(),
    Key.NumPad3 to Direction.DownRight(),
    Key.B to Direction.DownLeft(),
    Key.NumPad1 to Direction.DownLeft(),
    Key.Y to Direction.UpLeft(),
    Key.NumPad7 to Direction.UpLeft(),
    Key.U to Direction.UpRight(),
    Key.NumPad9 to Direction.UpRight(),
    Key.Period to Direction.Stationary(),
    Key.NumPad5 to Direction.Stationary(),
)

@OptIn(ExperimentalComposeUiApi::class)
val alphabeticalKeys = listOf(
    Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H,
    Key.I, Key.J, Key.K, Key.L, Key.M, Key.N, Key.O, Key.P,
    Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X,
    Key.Y, Key.Z,
)

@OptIn(ExperimentalComposeUiApi::class)
class WizardTowerGame {
    // Game Data:
    val tilemap = Tilemap.DebugArena()
    val camera = Camera(Coordinates(0, 0))
    var actors = mutableListOf<Actor>()
    var consumables = mutableListOf<Consumable>()
    val messageLog = MessageLog()
    var targetPathOverlayToggled = false
    var inputLock = false

    // Data to export to the GUI:
    var displayTiles by mutableStateOf(tilemap.exportTilesToCompose(camera.coordinates, tilemap.width, tilemap.height))
    var currentBackgroundColor by mutableStateOf(tilemap.backgroundColor)
    var overlayMode = OverlayType.NONE
    var inputMode = InputMode.NORMAL
    var displayMessages by mutableStateOf(messageLog.exportMessages())
    var turn by mutableStateOf(0)
    var playerDisplayStats by mutableStateOf(listOf<LabeledTextDataBundle>())
    var underCamera by mutableStateOf(defaultUnderCameraLabel)
    var underCameraHealth by mutableStateOf(defaultUnderCameraHealthLabel)
    var inventoryLabels by mutableStateOf(listOf<LabeledTextDataBundle>())
    var abilityLabels by mutableStateOf(listOf<LabeledTextDataBundle>())

    /**
     * Returns true if the game is over.
     *
     * todo: implement -- this is a placeholder.
     */
    fun gameOver(): Boolean {
        return false
    }

    /**
     * Returns the Actor at the given Coordinates or null.
     */
    private fun actorAtCoordinatesOrNull(coordinates: Coordinates): Actor? {
        return actors.firstOrNull { it.coordinates.matches(coordinates) }
    }

    /**
     * Returns the player from the actor pool.
     */
    fun getPlayer(): Actor {
        return actors
            .firstOrNull{ it.isPlayer }
            ?: error("Player not found.")
    }

    /**
     * Hands input off to the appropriate inputMode's corresponding function. If the input resulted in the need to
     * process a new turn then input is locked during this process.
     */
    fun handleInput(keyEvent: KeyEvent) {
        when (inputMode) {
            InputMode.NORMAL -> handleInputNormalMode(keyEvent)
            InputMode.INVENTORY -> handleInputInventoryMode(keyEvent)
            InputMode.ABILITIES -> handleInputAbilitiesMode(keyEvent)
        }

        if (inputLock) {
            processTurn()
        }
    }

    /**
     * Wraps everything which needs to happen after a turn advances. Still using a simple time system where player
     * goes first and all enemies go afterwards in a pretty arbitrary order. I will implement a more advanced time
     * system at some point.
     */
    private fun processTurn() {
        behaviorCheck()
        removeDeadActors()
        turn++
        syncGui()
        inputLock = false
    }

    /**
     * Runs the behavior function for each Actor in the game which has one.
     */
    private fun behaviorCheck() {
        actors
            .asSequence()
            .filter { it.behavior != null }
            .forEach { actor ->
                actor.behavior!!(this, actor)
            }
    }

    /**
     * Removes all dead actors and items from the game.
     */
    private fun removeDeadActors() {
        actors = actors
            .filter { it.isAlive() }
            .toMutableList()
    }

    /**
     * Handles user input when the Input Mode is set to the default.
     */
    fun handleInputNormalMode(keyEvent: KeyEvent) {
        var moved = false

        val player = getPlayer()

        when (keyEvent.key in movementKeyDirectionMap.keys) {
            true -> {
                // Movement keys using Vi keys or the NumPad:
                if (camera.coupledToOrNull == null) {
                    camera.move(movementKeyDirectionMap[keyEvent.key]!!, tilemap)
                    syncGui()
                }
                else if (player.move(movementKeyDirectionMap[keyEvent.key]!!, this)) {
                    moved = true

                    // Movement triggers a new turn:
                    inputLock = true
                }
            }
            else -> {
                when (keyEvent.key) {
                    // Toggle manual targeting mode:
                    Key.X -> {
                        if (camera.coupledToOrNull != null) {
                            camera.decouple()
                            messageLog.addMessage(Message(turn, "Manual targeting mode enabled.", White))
                        } else {
                            camera.coupleTo(getPlayer())
                            messageLog.addMessage(Message(turn, "Manual targeting mode disabled.", White))
                        }
                        syncGui()
                    }

                    // Toggle "path" overlay for a target in manual targeting mode:
                    Key.P -> {
                        // Only works if the camera is decoupled (manual targeting mode):
                        if (camera.coupledToOrNull == null) {
                            targetPathOverlayToggled = !targetPathOverlayToggled
                            messageLog.addMessage(Message(turn, "Toggled path-to-target overlay.", White))
                            syncGui()
                        }
                    }

                    // Tab for auto-targeting:
                    Key.Tab -> {
                        // Only works if the camera is decoupled (manual targeting mode):
                        if (camera.coupledToOrNull == null) {

                            // All actors in sight, ordered by distance from the player:
                            val actorsInSight = actors
                                .asSequence()
                                .filter { player.canSee(it.coordinates, this) }
                                .sortedBy { player.coordinates.chebyshevDistance(it.coordinates) }
                                .toList()

                            if (actorsInSight.isEmpty())
                                return

                            // Get index of "current target" if one selected:
                            var targetIndex = actorAtCoordinatesOrNull(camera.coordinates)
                                ?.let { target ->
                                    actorsInSight.indices
                                        .firstOrNull { actorsInSight[it] == target }
                                        ?: error("This should never happen.")
                                }
                                ?: 0

                            // Cycle through the list:
                            targetIndex = targetIndex
                                .plus(1)
                                .mod(actorsInSight.size)

                            // Snap camera to the next target:
                            camera.snapTo(actorsInSight[targetIndex].coordinates)

                            syncGui()
                        }
                    }

                    // Normal overlay:
                    Key.F1 -> {
                        overlayMode = OverlayType.NONE
                        messageLog.addMessage(Message(turn, "Reset overlay mode.", White))
                        syncGui()
                    }

                    // Actor Faction overlay:
                    Key.F2 -> {
                        overlayMode = OverlayType.FACTION
                        messageLog.addMessage(Message(turn, "Activated Actor Faction overlay mode.", White))
                        syncGui()
                    }

                    // Passable Tiles overlay:
                    Key.F3 -> {
                        overlayMode = OverlayType.PASSABLE
                        messageLog.addMessage(Message(turn, "Activated Passable Tiles overlay mode.", White))
                        syncGui()
                    }

                    // Toggle the Inventory Mode:
                    Key.I -> {
                        inputMode = InputMode.INVENTORY
                        inventoryLabels = (getPlayer() as Actor.Player).exportInventoryStrings()
                        messageLog.addMessage(Message(turn, "Inventory Input Mode toggled (ESC to return).", White))
                        syncGui()
                    }

                    // Toggle the Abilities Mode:
                    Key.A -> {
                        inputMode = InputMode.ABILITIES
                        abilityLabels = (getPlayer() as Actor.Player).exportAbilityStrings()
                        messageLog.addMessage(Message(turn, "Abilities Input Mode toggled (ESC to return).", White))
                        syncGui()
                    }
                }
            }
        }

        // If the player moved, then describe the Tile they landed on:
        // todo: descriptions not really implemented yet
        if (moved)
            tilemap.getTileOrNull(player.coordinates)
                ?.let { it.describe().forEach { messageLog.addMessage(it) } }
                ?: error("Player not found.")
    }

    /**
     * Handles user input for the Abilities Input Mode.
     */
    fun handleInputAbilitiesMode(
        keyEvent: KeyEvent,
    ) {
        when (keyEvent.key in alphabeticalKeys) {
            true -> {
                // Get the index of the item selected:
                val abilitiesIndex = alphabeticalKeys
                    .zip(0 until MAX_INVENTORY_SIZE)
                    .first { it.first == keyEvent.key }
                    .second

                val player = getPlayer()

                // Out-of-bounds check:
                if (abilitiesIndex >= player.abilities.size)
                    return

                val ability = player.abilities[abilitiesIndex]

                val targetOrNull = actorAtCoordinatesOrNull(camera.coordinates)

                ability.effect(this, player, targetOrNull)

                // Send the player back to the main interface afterwards:
                inputMode = InputMode.NORMAL

                // Counts as a turn:
                inputLock = true
            }
            else -> {
                if (keyEvent.key == Key.Escape) {
                    inputMode = InputMode.NORMAL
                    syncGui()
                }
            }
        }
    }

    /**
     * Handles user input for the Inventory Input Mode.
     */
    fun handleInputInventoryMode(keyEvent: KeyEvent) {
        when (keyEvent.key in alphabeticalKeys) {
            true -> {
                // Get the index of the item selected:
                val inventoryIndex = alphabeticalKeys
                    .zip(0 until MAX_INVENTORY_SIZE)
                    .first { it.first == keyEvent.key }
                    .second

                val player = getPlayer()

                // Out-of-bounds check:
                if (inventoryIndex >= player.inventory!!.size)
                    return

                val item = player.inventory!![inventoryIndex]

                val targetOrNull = actorAtCoordinatesOrNull(camera.coordinates)

                item.use(this, player, targetOrNull)

                // Send the player back to the main interface afterwards:
                inputMode = InputMode.NORMAL

                // Counts as a turn:
                inputLock = true
            }
            else -> {
                if (keyEvent.key == Key.Escape) {
                    inputMode = InputMode.NORMAL
                    syncGui()
                }
            }
        }
    }

    /**
     * Overlays all the Tiles in the display with their isPassable status.
     */
    private fun overlayPassableTiles(): List<List<CellDisplayBundle>> {
        val displayDimensions = Pair(mapDisplayWidthNormal, mapDisplayHeightNormal)

        return tilemap
            .exportTilesToCompose(camera.coordinates, displayDimensions.first, displayDimensions.second)
            .map { row ->
                row.map { cell ->
                    tilemap.getTileOrNull(cell.coordinates)
                        ?.let{ tile ->
                            CellDisplayBundle(
                                displayValue = tile.displayValue,
                                displayColor = when (tile.isPassable) {
                                    true -> GoGreen
                                    else -> AlertRed
                                },
                                coordinates = tile.coordinates
                            )
                        }
                        ?: cell
                }
            }
    }

    /**
     * Overlays the Actors in play on top of the tiles exported from the Tilemap and sets the
     * displayTiles variable which is used by the interface.
     */
    private fun overlayActorsOnDisplayTiles() {
        val player = getPlayer()

        // Calculate the Field of View:
        tilemap.calculateFieldOfView(player, this)

        // Grab exported tiles w/ a potential overlay:
        val displayDimensions = Pair(mapDisplayWidthNormal, mapDisplayHeightNormal)

        // Export tiles from Tilemap with a potential overlay:
        val newTiles = when (overlayMode) {
            OverlayType.PASSABLE -> overlayPassableTiles()
            else -> tilemap.exportTilesToCompose(camera.coordinates, displayDimensions.first, displayDimensions.second)
        }

        // Grab the Target Path overlay if it is toggled:
        val targetLineOrNull = when (targetPathOverlayToggled) {
            true -> player.coordinates
                .bresenhamLineTo(camera.coordinates)
                .filter { !it.matches(player.coordinates) && !it.matches(camera.coordinates) }
            else -> null
        }

        // Apply the Tiles and Overlays with the Actors on top of that:
        displayTiles = newTiles
            .map { row ->
                row.map { cell ->
                    tilemap
                        .getTileOrNull(cell.coordinates)
                        ?.let { tile ->
                            // If the Target Path is to be overlaid on this tile:
                            if (targetLineOrNull != null && targetLineOrNull.any { it.matches(tile.coordinates) })
                                targetedTile(tile.coordinates, "*")

                            // Else if player can see tile:
                            else if (tile.visibleToPlayer)
                                actors
                                    .firstOrNull { it.coordinates.matches(cell.coordinates) }
                                    .let { maybeActor ->
                                        maybeActor
                                            // If there is an Actor on the tile:
                                            ?.toCellDisplayBundle(
                                                overlayMode = overlayMode,
                                                decoupledCamera = when (camera.coupledToOrNull != null) {
                                                    true -> null
                                                    else -> camera.coordinates
                                                }
                                            )
                                            // When there is no actor:
                                            ?: when (camera.coupledToOrNull == null) {
                                                true ->
                                                    // Crosshairs for targeted Tile in manual targeting mode:
                                                    if (tile.coordinates.matches(camera.coordinates))
                                                        targetedTile(tile.coordinates, "X")
                                                    else
                                                        cell
                                                else -> cell
                                            }
                                    }
                            // Else if player can't see Tile, but they are targeting it manually:
                            else if (camera.coupledToOrNull == null && tile.coordinates.matches(camera.coordinates))
                                targetedTile(tile.coordinates, "X")
                            else
                                cell
                        }
                        ?: cell
                }
            }
    }

    /**
     * Makes sure the GUI and the Game are in-sync.
     */
    fun syncGui() {
        val player = getPlayer() as Actor.Player

        // Snap the camera to anything it is coupled to:
        camera.snap()

        // Overlay visible actors and calculate FoV:
        overlayActorsOnDisplayTiles()

        // Prepare the Bottom Console:
        displayMessages = messageLog.exportMessages()

        // Prepare the player's stats for the HUD:
        playerDisplayStats = player.exportStatsToCompose()

        // If the player has anything under the target cursor then some info for the HUD:
        when (val maybeActor = actorAtCoordinatesOrNull(camera.coordinates)) {
            null -> {
                underCamera = defaultUnderCameraLabel
                underCameraHealth = defaultUnderCameraHealthLabel
            }
            else -> {
                underCamera =
                    LabeledTextDataBundle("Under Camera", maybeActor.name, factionColors[maybeActor.maybeFaction]!!)
                underCameraHealth =
                    LabeledTextDataBundle("Target Health", "${maybeActor.health}/${maybeActor.maxHealth}", White)
            }
        }
    }

    /**
     * Adds the player to the game.
     */
    private fun addActor(actor: Actor) {
        actors.add(actor)
    }

    init {
        /*
            For now, starting off with just a player in a test arena to work on mechanics and systems.
         */
        addActor(
            Actor.Player(
                tilemap
                    .randomTileOfType(TileType.FLOOR)
                    .coordinates
            )
        )
        camera.coupleTo(getPlayer())

        val player = getPlayer()

        // For now, I'll start the player with some simple abilities/spells for testing:
        player.addAbility(Ability.MinorHealSelf())
        player.addAbility(Ability.MagicMissile())

        // Give the player 5 potions to play with.
        val numStartingPotions = 5
        repeat (numStartingPotions) {
            player.addConsumable(
                Consumable(
                    name = "Minor Healing Potion",
                    ability = Ability.MinorHealSelf(),
                    charges = 1,
                    maxCharges = 1,
                    containedWithin = player.inventory!!
                )
            )
        }

        // Place a Barg close to the player for testing:
        val bargSpawn = tilemap.tilesInRadius(player.coordinates, 5)
            .filter { it.isPassable }
            .random()
            .coordinates
        addActor(
            Actor.Barg(bargSpawn)
        )

        syncGui()
    }
}