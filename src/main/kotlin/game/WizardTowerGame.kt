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
    // more to come
}

@OptIn(ExperimentalComposeUiApi::class)
class WizardTowerGame {
    // Game Data:
    val tilemap = Tilemap.DebugArena()
    var camera = Coordinates(tilemap.width / 2, tilemap.height / 2)
    val actors = mutableListOf<Actor>()
    val messageLog = MessageLog()

    // Data to export to the GUI:
    var displayTiles by mutableStateOf(tilemap.exportTilesToCompose(camera))
    var currentBackgroundColor by mutableStateOf(tilemap.backgroundColor)
    var overlayMode = OverlayType.NONE
    var displayMessages by mutableStateOf(messageLog.exportMessages())
    var turn by mutableStateOf(0)
    var playerDisplayStats by mutableStateOf(listOf<LabeledTextDataBundle>())

    /**
     * Returns true if the game is over.
     *
     * todo: implement -- this is a placeholder.
     */
    fun gameOver(): Boolean {
        return false
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
     */
    fun handleInput(keyEvent: KeyEvent) {
        var turnAdvanced = false
        var moved = false

        val player = getPlayer()

        when (keyEvent.key) {
            // Movement keys using Vi keys or the NumPad:
            Key.H -> {
                if (player.move(Direction.Left(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad4 -> {
                if (player.move(Direction.Left(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.J -> {
                if (player.move(Direction.Down(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad2 -> {
                if (player.move(Direction.Down(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.K -> {
                if (player.move(Direction.Up(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad8 -> {
                if (player.move(Direction.Up(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.L -> {
                if (player.move(Direction.Right(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad6 -> {
                if (player.move(Direction.Right(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.Y -> {
                if (player.move(Direction.UpLeft(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad7 -> {
                if (player.move(Direction.UpLeft(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.U -> {
                if (player.move(Direction.UpRight(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad9 -> {
                if (player.move(Direction.UpRight(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.B -> {
                if (player.move(Direction.DownLeft(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad1 -> {
                if (player.move(Direction.DownLeft(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.N -> {
                if (player.move(Direction.DownRight(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.NumPad3 -> {
                if (player.move(Direction.DownRight(), tilemap)) {
                    moved = true
                    turnAdvanced = true
                }
            }
            Key.Period -> {
                if (player.move(Direction.Stationary(), tilemap))
                    turnAdvanced = true
            }
            Key.NumPad5-> {
                if (player.move(Direction.Stationary(), tilemap))
                    turnAdvanced = true
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
            .exportTilesToCompose(camera)
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
            else -> tilemap.exportTilesToCompose(camera)
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
                                            ?.toCellDisplayBundle(overlayMode)
                                            ?: cell
                                    }
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
        camera = player.coordinates
        overlayActorsOnDisplayTiles()
        displayMessages = messageLog.exportMessages()
        playerDisplayStats = player.exportToCompose()
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
        setGui()
    }
}