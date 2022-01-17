package game

import inputoutput.CellDisplayBundle
import inputoutput.mapDisplayHeightNormal
import inputoutput.mapDisplayWidthNormal

/**
 * A Scene class to encapsulate the parts of the game which can be swapped out from game area to game area.
 */
sealed class Scene(
    val tilemap: Tilemap,
    val camera: Camera,
    var actors: MutableList<Actor>,
    var consumables: MutableList<Consumable>,
) {
    /**
     * Adds the player to the game.
     */
    fun addActor(actor: Actor) {
        actors.add(actor)
    }

    /**
     * Returns true if the camera is coupled to an Actor.
     */
    fun cameraCoupled(): Boolean {
        return camera.coupledToOrNull != null
    }

    /**
     * Exports the slice of the Tilemap which is to be displayed.
     */
    fun exportDisplayTiles(): List<List<CellDisplayBundle>> {
        return tilemap.exportTilesToCompose(camera.coordinates, mapDisplayWidthNormal, mapDisplayHeightNormal)
    }

    /**
     * Moves the camera in the given direction/
     */
    fun moveCamera(direction: Direction) {
        camera.move(direction, tilemap)
    }

    /**
     * Removes all dead actors and items from the game.
     */
    fun removeDeadActors() {
        actors = actors
            .filter { it.isAlive() }
            .toMutableList()
    }

    class DebugArena : Scene(
        tilemap = Tilemap.DebugArena(),
        camera = Camera(Coordinates(0, 0)),
        actors = mutableListOf(),
        consumables = mutableListOf(),
    )
}