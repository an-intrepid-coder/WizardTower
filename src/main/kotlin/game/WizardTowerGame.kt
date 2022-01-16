package game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import inputoutput.*

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
    BIND_KEY,
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
class WizardTowerGame {
    // Game Data:
    val tilemap = Tilemap.DebugArena()
    val camera = Camera(Coordinates(0, 0))
    var actors = mutableListOf<Actor>()
    var consumables = mutableListOf<Consumable>()
    val messageLog = MessageLog()
    var targetPathOverlayToggled = false
    var inputLocked = false
    val gameKeys = GameKeys()
    var maybeRebindingKey: GameKeyLabel? = null

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
            InputMode.BIND_KEY -> handleInputBindKeyMode(keyEvent)
        }
        if (inputLocked)
            processTurn()
    }

    /**
     * Handles user input when the Input Mode is set to Bind Key.
     */
    private fun handleInputBindKeyMode(keyEvent: KeyEvent) {
        if (keyEvent.isShiftPressed) {
            gameKeys.rebindKey(maybeRebindingKey!!, keyEvent.key)

            messageLog.addMessage(
                Message(
                    turn = turn,
                    text = "$maybeRebindingKey bound to ${keyEvent.key}.",
                    textColor = BrightBlue,
                )
            )

            inputMode = InputMode.NORMAL
            maybeRebindingKey = null
            syncGui()
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
        inputLocked = false
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
    private fun handleInputNormalMode(keyEvent: KeyEvent) {
        if (keyEvent.isCtrlPressed && keyEvent.key in gameKeys.rebindableKeymap.values) {
            maybeRebindingKey = gameKeys.gameKeyLabelFromBoundKeyOrNull(keyEvent.key)
            inputMode = InputMode.BIND_KEY

            messageLog.addMessage(
                Message(
                    turn = turn,
                    text = "Bind Key Mode toggled for $maybeRebindingKey. Press shift + desired key.",
                    textColor = BrightBlue,
                )
            )
            syncGui()
            return
        }

        var moved = false
        val player = getPlayer()
        val directionOrNull = gameKeys.directionFromKeyOrNull(keyEvent.key)
        when (directionOrNull != null) {
            true -> {
                // Movement keys using Vi keys or the NumPad:
                if (camera.coupledToOrNull == null) {
                    camera.move(directionOrNull, tilemap)
                    syncGui()
                }
                else if (player.move(directionOrNull, this)) {
                    moved = true

                    // Movement triggers a new turn:
                    inputLocked = true
                }
            }
            else -> {
                when (gameKeys.gameKeyLabelFromBoundKeyOrNull(keyEvent.key)) {
                    // Toggle manual targeting mode:
                    GameKeyLabel.TOGGLE_MANUAL_CAMERA -> {
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
                    GameKeyLabel.TOGGLE_TARGET_PATH -> {
                        // Only works if the camera is decoupled (manual targeting mode):
                        if (camera.coupledToOrNull == null) {
                            targetPathOverlayToggled = !targetPathOverlayToggled
                            messageLog.addMessage(Message(turn, "Toggled path-to-target overlay.", White))
                            syncGui()
                        }
                    }

                    // Tab for auto-targeting:
                    GameKeyLabel.AUTO_TARGET -> {
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
                    GameKeyLabel.RESET_MAP_OVERLAY -> {
                        overlayMode = OverlayType.NONE
                        messageLog.addMessage(Message(turn, "Reset overlay mode.", White))
                        syncGui()
                    }

                    // Actor Faction overlay:
                    GameKeyLabel.FACTION_MAP_OVERLAY -> {
                        overlayMode = OverlayType.FACTION
                        messageLog.addMessage(Message(turn, "Activated Actor Faction overlay mode.", White))
                        syncGui()
                    }

                    // Passable Tiles overlay:
                    GameKeyLabel.PASSABLE_TERRAIN_MAP_OVERLAY -> {
                        overlayMode = OverlayType.PASSABLE
                        messageLog.addMessage(Message(turn, "Activated Passable Tiles overlay mode.", White))
                        syncGui()
                    }

                    // Toggle the Inventory Mode:
                    GameKeyLabel.INVENTORY_MENU -> {
                        inputMode = InputMode.INVENTORY
                        inventoryLabels = (getPlayer() as Actor.Player).exportInventoryStrings()
                        messageLog.addMessage(Message(turn, "Inventory Input Mode toggled (ESC to return).", White))
                        syncGui()
                    }

                    // Toggle the Abilities Mode:
                    GameKeyLabel.ABILITIES_MENU -> {
                        inputMode = InputMode.ABILITIES
                        abilityLabels = (getPlayer() as Actor.Player).exportAbilityStrings()
                        messageLog.addMessage(Message(turn, "Abilities Input Mode toggled (ESC to return).", White))
                        syncGui()
                    }

                    else -> Unit
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
    private fun handleInputAbilitiesMode(
        keyEvent: KeyEvent,
    ) {
        when (keyEvent.key in gameKeys.alphabeticalKeys) {
            true -> {
                // Get the index of the item selected:
                val abilitiesIndex = gameKeys.alphabeticalKeys
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
                inputLocked = true
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
    private fun handleInputInventoryMode(keyEvent: KeyEvent) {
        when (keyEvent.key in gameKeys.alphabeticalKeys) {
            true -> {
                // Get the index of the item selected:
                val inventoryIndex = gameKeys.alphabeticalKeys
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
                inputLocked = true
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
    private fun syncGui() {
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