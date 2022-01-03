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
    // todo: Implement overlays
    NONE,
    FACTION,
    PASSABLE,
    // more to come -- especially for environmental effects and complex spell interactions, eventually
}

enum class OverwindowType {
    TITLE_SCREEN,
    MAP_SCREEN,
    CHARACTER_SCREEN,
    INVENTORY_SCREEN,
    POP_UP_ALERT,
    CONVERSATION_WINDOW,
    // more to come
}

fun targetedTile(coordinates: Coordinates): CellDisplayBundle {
    return CellDisplayBundle(
        displayValue = "X",
        displayColor = BrightPurple,
        coordinates = coordinates,
    )
}

val defaultUnderCameraLabel = LabeledTextDataBundle("Under Camera", "N/A", White)

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
class WizardTowerGame {
    // Game Data:
    val tilemap = Tilemap.DebugArena()
    val camera = Camera(Coordinates(0, 0))
    val actors = mutableListOf<Actor>()
    val messageLog = MessageLog()

    // Data to export to the GUI:
    var displayTiles by mutableStateOf(tilemap.exportTilesToCompose(camera.coordinates))
    var currentBackgroundColor by mutableStateOf(tilemap.backgroundColor)
    var overlayMode = OverlayType.NONE
    var displayMessages by mutableStateOf(messageLog.exportMessages())
    var turn by mutableStateOf(0)
    var playerDisplayStats by mutableStateOf(listOf<LabeledTextDataBundle>())
    var underCamera by mutableStateOf(defaultUnderCameraLabel)

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
    private fun getPlayer(): Actor {
        return actors
            .firstOrNull{ it.isPlayer }
            ?: error("Player not found.")
    }

    /**
     * When input happens in the application window, it is sent to this function. All keyEvents are assumed to be
     * KeyUp, for now.
     *
     * todo: For the Pop-Up menus, I will want a separate handleInput function to take over from this one while
     *  pop-ups are active. I will probably rename this one at that point.
     */
    fun handleInput(keyEvent: KeyEvent) {
        var turnAdvanced = false
        var moved = false

        val player = getPlayer()

        when (keyEvent.key in movementKeyDirectionMap.keys) {
            true -> {
                // Movement keys using Vi keys or the NumPad:
                if (camera.coupledToOrNull == null)
                    camera.move(movementKeyDirectionMap[keyEvent.key]!!, tilemap)
                else if (player.move(movementKeyDirectionMap[keyEvent.key]!!, tilemap)) {
                    moved = true
                    turnAdvanced = true
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
                    }

                    // Normal overlay:
                    Key.F1 -> {
                        overlayMode = OverlayType.NONE
                        messageLog.addMessage(Message(turn, "Reset overlay mode.", White))
                    }

                    // Actor Faction overlay:
                    Key.F2 -> {
                        overlayMode = OverlayType.FACTION
                        messageLog.addMessage(Message(turn, "Activated Actor Faction overlay mode.", White))
                    }

                    // Passable Tiles overlay:
                    Key.F3 -> {
                        overlayMode = OverlayType.PASSABLE
                        messageLog.addMessage(Message(turn, "Activated Passable Tiles overlay mode.", White))
                    }
                }
            }
        }

        if (moved)
            tilemap.getTileOrNull(player.coordinates)
                ?.let { it.describe().forEach { messageLog.addMessage(it) } }
                ?: error("Player not found.")

        if (turnAdvanced) {
            // todo: <various systems will go right here>
            turn++
        }

        setGui()
    }

    /**
     * Overlays all the Tiles in the display with their isPassable status.
     */
    private fun overlayPassableTiles(): List<List<CellDisplayBundle>> {
        return tilemap
            .exportTilesToCompose(camera.coordinates)
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
        // Calculate the Field of View:
        tilemap.calculateFieldOfView(getPlayer())

        // Grab exported tiles w/ a potential overlay:
        val newTiles = when (overlayMode) {
            OverlayType.PASSABLE -> overlayPassableTiles()
            else -> tilemap.exportTilesToCompose(camera.coordinates)
        }

        // Apply the potential overlay with the Actors on top of that:
        displayTiles = newTiles
            .map { row ->
                row.map { cell ->
                    tilemap
                        .getTileOrNull(cell.coordinates)
                        ?.let { tile ->
                            if (tile.visibleToPlayer)
                                actors
                                    .firstOrNull { it.coordinates.matches(cell.coordinates) }
                                    .let { maybeActor ->
                                        maybeActor
                                            ?.toCellDisplayBundle(
                                                overlayMode = overlayMode,
                                                decoupledCamera = when (camera.coupledToOrNull != null) {
                                                    true -> null
                                                    else -> camera.coordinates
                                                }
                                            )
                                            ?: when (camera.coupledToOrNull == null) {
                                                true -> if (tile.coordinates.matches(camera.coordinates))
                                                    targetedTile(tile.coordinates)
                                                else
                                                    cell
                                                else -> cell
                                            }
                                    }
                            else if (camera.coupledToOrNull == null && tile.coordinates.matches(camera.coordinates))
                                targetedTile(tile.coordinates)
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
    private fun setGui() {
        val player = getPlayer() as Actor.Player

        camera.snap()

        overlayActorsOnDisplayTiles()

        displayMessages = messageLog.exportMessages()

        playerDisplayStats = player.exportToCompose()

        underCamera = actorAtCoordinatesOrNull(camera.coordinates)
            ?.let { actor ->
                LabeledTextDataBundle("Under Camera", actor.name, factionColors[actor.maybeFaction]!!)
            }
            ?: defaultUnderCameraLabel
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
        setGui()
    }
}