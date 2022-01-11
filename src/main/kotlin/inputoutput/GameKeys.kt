package inputoutput

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import game.Direction

/**
 * An enumeration of all specific input commands.
 */
enum class GameKey {
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

/**
 * A map of all input enumerations to their defaults.
 *
 * todo: next: a settings interface for rebinding.
 */
@OptIn(ExperimentalComposeUiApi::class)
val rebindableKeymap = mutableMapOf(
    GameKey.MOVE_STATIONARY to Key.Period,
    GameKey.ALT_MOVE_STATIONARY to Key.NumPad5,

    GameKey.MOVE_UP to Key.K,
    GameKey.ALT_MOVE_UP to Key.NumPad8,

    GameKey.MOVE_UPLEFT to Key.Y,
    GameKey.ALT_MOVE_UPLEFT to Key.NumPad7,

    GameKey.MOVE_UPRIGHT to Key.U,
    GameKey.ALT_MOVE_UPRIGHT to Key.NumPad9,

    GameKey.MOVE_DOWN to Key.J,
    GameKey.ALT_MOVE_DOWN to Key.NumPad2,

    GameKey.MOVE_DOWNLEFT to Key.B,
    GameKey.ALT_MOVE_DOWNLEFT to Key.NumPad1,

    GameKey.MOVE_DOWNRIGHT to Key.N,
    GameKey.ALT_MOVE_DOWNRIGHT to Key.NumPad3,

    GameKey.MOVE_RIGHT to Key.L,
    GameKey.ALT_MOVE_RIGHT to Key.NumPad6,

    GameKey.MOVE_LEFT to Key.H,
    GameKey.ALT_MOVE_LEFT to Key.NumPad4,

    GameKey.ESCAPE to Key.Escape,

    GameKey.RESET_MAP_OVERLAY to Key.F1,
    GameKey.FACTION_MAP_OVERLAY to Key.F2,
    GameKey.PASSABLE_TERRAIN_MAP_OVERLAY to Key.F3,

    GameKey.TOGGLE_MANUAL_CAMERA to Key.X,
    GameKey.AUTO_TARGET to Key.Tab,
    GameKey.TOGGLE_TARGET_PATH to Key.P,

    GameKey.ABILITIES_MENU to Key.A,
    GameKey.INVENTORY_MENU to Key.I,
)

/**
 * Returns an appropriate Direction from a movement key, or null.
 */
fun directionFromKeyOrNull(key: Key): Direction? {
    val movementKeysToDirections = mapOf(
        rebindableKeymap[GameKey.MOVE_STATIONARY] to Direction.Stationary(),
        rebindableKeymap[GameKey.ALT_MOVE_STATIONARY] to Direction.Stationary(),

        rebindableKeymap[GameKey.MOVE_UP] to Direction.Up(),
        rebindableKeymap[GameKey.ALT_MOVE_UP] to Direction.Up(),

        rebindableKeymap[GameKey.MOVE_DOWN] to Direction.Down(),
        rebindableKeymap[GameKey.ALT_MOVE_DOWN] to Direction.Down(),

        rebindableKeymap[GameKey.MOVE_RIGHT] to Direction.Right(),
        rebindableKeymap[GameKey.ALT_MOVE_RIGHT] to Direction.Right(),

        rebindableKeymap[GameKey.MOVE_UPLEFT] to Direction.UpLeft(),
        rebindableKeymap[GameKey.ALT_MOVE_UPLEFT] to Direction.UpLeft(),

        rebindableKeymap[GameKey.MOVE_UPRIGHT] to Direction.UpRight(),
        rebindableKeymap[GameKey.ALT_MOVE_UPRIGHT] to Direction.UpRight(),

        rebindableKeymap[GameKey.MOVE_LEFT] to Direction.Left(),
        rebindableKeymap[GameKey.ALT_MOVE_LEFT] to Direction.Left(),

        rebindableKeymap[GameKey.MOVE_DOWNLEFT] to Direction.DownLeft(),
        rebindableKeymap[GameKey.ALT_MOVE_DOWNLEFT] to Direction.DownLeft(),

        rebindableKeymap[GameKey.MOVE_DOWNRIGHT] to Direction.DownRight(),
        rebindableKeymap[GameKey.ALT_MOVE_DOWNRIGHT] to Direction.DownRight(),
    )
    return movementKeysToDirections[key]
}

/**
 * All alphabet keys.
 */
@OptIn(ExperimentalComposeUiApi::class)
val alphabeticalKeys = listOf(
    Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H,
    Key.I, Key.J, Key.K, Key.L, Key.M, Key.N, Key.O, Key.P,
    Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X,
    Key.Y, Key.Z,
)