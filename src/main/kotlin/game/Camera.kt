package game

class Camera(
    var coordinates: Coordinates,
    var coupledToOrNull: Actor? = null,
) {

    /**
     * "Snaps" the camera to the given newCoordiantes.
     */
    fun snapTo(newCoordinates: Coordinates) {
        coordinates = newCoordinates
    }

    /**
     * Snaps to the coupled Actor (does nothing if no coupled actor).
     */
    fun snap() {
        coordinates = coupledToOrNull?.coordinates ?: coordinates
    }

    /**
     * Couples the Camera to the given actor until decoupled.
     */
    fun coupleTo(actor: Actor) {
        coupledToOrNull = actor
    }

    /**
     * Decouples the camera from an actor.
     */
    fun decouple() {
            coupledToOrNull = null
    }
}