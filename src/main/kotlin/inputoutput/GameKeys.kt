package inputoutput

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import game.Direction

/**
 * An enumeration of all specific input commands.
 */
enum class GameKeyLabel {
    MOVE_STATIONARY,
    ALT_MOVE_STATIONARY,

    MOVE_UP,
    ALT_MOVE_UP,

    MOVE_UPLEFT,
    ALT_MOVE_UPLEFT,

    MOVE_UPRIGHT,
    ALT_MOVE_UPRIGHT,

    MOVE_DOWN,
    ALT_MOVE_DOWN,

    MOVE_DOWNLEFT,
    ALT_MOVE_DOWNLEFT,

    MOVE_DOWNRIGHT,
    ALT_MOVE_DOWNRIGHT,

    MOVE_RIGHT,
    ALT_MOVE_RIGHT,

    MOVE_LEFT,
    ALT_MOVE_LEFT,

    ESCAPE,

    RESET_MAP_OVERLAY,
    FACTION_MAP_OVERLAY,
    PASSABLE_TERRAIN_MAP_OVERLAY,

    TOGGLE_MANUAL_CAMERA,

    AUTO_TARGET,

    TOGGLE_TARGET_PATH,

    ABILITIES_MENU,

    INVENTORY_MENU,
}

@OptIn(ExperimentalComposeUiApi::class)
class GameKeys {
    /**
     * All alphabet keys.
     */
    val alphabeticalKeys = listOf(
        Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H,
        Key.I, Key.J, Key.K, Key.L, Key.M, Key.N, Key.O, Key.P,
        Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X,
        Key.Y, Key.Z,
    )

    /**
     * A map of all input enumerations to their defaults.
     */
    val rebindableKeymap = mutableMapOf(
        GameKeyLabel.MOVE_STATIONARY to Key.Period,
        GameKeyLabel.ALT_MOVE_STATIONARY to Key.NumPad5,

        GameKeyLabel.MOVE_UP to Key.K,
        GameKeyLabel.ALT_MOVE_UP to Key.NumPad8,

        GameKeyLabel.MOVE_UPLEFT to Key.Y,
        GameKeyLabel.ALT_MOVE_UPLEFT to Key.NumPad7,

        GameKeyLabel.MOVE_UPRIGHT to Key.U,
        GameKeyLabel.ALT_MOVE_UPRIGHT to Key.NumPad9,

        GameKeyLabel.MOVE_DOWN to Key.J,
        GameKeyLabel.ALT_MOVE_DOWN to Key.NumPad2,

        GameKeyLabel.MOVE_DOWNLEFT to Key.B,
        GameKeyLabel.ALT_MOVE_DOWNLEFT to Key.NumPad1,

        GameKeyLabel.MOVE_DOWNRIGHT to Key.N,
        GameKeyLabel.ALT_MOVE_DOWNRIGHT to Key.NumPad3,

        GameKeyLabel.MOVE_RIGHT to Key.L,
        GameKeyLabel.ALT_MOVE_RIGHT to Key.NumPad6,

        GameKeyLabel.MOVE_LEFT to Key.H,
        GameKeyLabel.ALT_MOVE_LEFT to Key.NumPad4,

        GameKeyLabel.ESCAPE to Key.Escape,

        GameKeyLabel.RESET_MAP_OVERLAY to Key.F1,
        GameKeyLabel.FACTION_MAP_OVERLAY to Key.F2,
        GameKeyLabel.PASSABLE_TERRAIN_MAP_OVERLAY to Key.F3,

        GameKeyLabel.TOGGLE_MANUAL_CAMERA to Key.X,
        GameKeyLabel.AUTO_TARGET to Key.Tab,
        GameKeyLabel.TOGGLE_TARGET_PATH to Key.P,

        GameKeyLabel.ABILITIES_MENU to Key.A,
        GameKeyLabel.INVENTORY_MENU to Key.I,
    )

    /**
     * Returns the GameKeyLabel bound to the given key or null.
     */
    fun gameKeyLabelFromBoundKeyOrNull(key: Key): GameKeyLabel? {
        for (entry in rebindableKeymap) {
            if (entry.value == key)
                return entry.key
        }
        return null
    }

    /**
     * Returns an appropriate Direction from a movement key, or null.
     */
    fun directionFromKeyOrNull(key: Key): Direction? {
        val movementKeysToDirections = mapOf(
            rebindableKeymap[GameKeyLabel.MOVE_STATIONARY] to Direction.Stationary(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_STATIONARY] to Direction.Stationary(),

            rebindableKeymap[GameKeyLabel.MOVE_UP] to Direction.Up(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_UP] to Direction.Up(),

            rebindableKeymap[GameKeyLabel.MOVE_DOWN] to Direction.Down(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_DOWN] to Direction.Down(),

            rebindableKeymap[GameKeyLabel.MOVE_RIGHT] to Direction.Right(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_RIGHT] to Direction.Right(),

            rebindableKeymap[GameKeyLabel.MOVE_UPLEFT] to Direction.UpLeft(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_UPLEFT] to Direction.UpLeft(),

            rebindableKeymap[GameKeyLabel.MOVE_UPRIGHT] to Direction.UpRight(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_UPRIGHT] to Direction.UpRight(),

            rebindableKeymap[GameKeyLabel.MOVE_LEFT] to Direction.Left(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_LEFT] to Direction.Left(),

            rebindableKeymap[GameKeyLabel.MOVE_DOWNLEFT] to Direction.DownLeft(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_DOWNLEFT] to Direction.DownLeft(),

            rebindableKeymap[GameKeyLabel.MOVE_DOWNRIGHT] to Direction.DownRight(),
            rebindableKeymap[GameKeyLabel.ALT_MOVE_DOWNRIGHT] to Direction.DownRight(),
        )
        return movementKeysToDirections[key]
    }

    /**
     * Rebinds the given game control key to a different physical key.
     */
    fun rebindKey(
        gameKeyLabel: GameKeyLabel,
        newKey: Key,
    ) {
        val alreadyBoundOrNull = gameKeyLabelFromBoundKeyOrNull(newKey)
        if (alreadyBoundOrNull == null)
            rebindableKeymap[gameKeyLabel] = newKey
        else {
            rebindableKeymap[alreadyBoundOrNull] = rebindableKeymap[gameKeyLabel]!!
            rebindableKeymap[gameKeyLabel] = newKey
        }
    }
}
