package game

import java.util.*

// Wikipedia pcode uses an "infinite" value for this, but this is effectively infinite for now:
const val scoreDefault = Int.MAX_VALUE / 2

/**
 * Finds the shortest path from one set of Coordinates to another using the A* search algorithm.
 * Can be injected with waypoints and custom heuristics.
 *
 * (work in progress)
 */
sealed class AStarPath(
    waypoints: List<Coordinates>,
    game: WizardTowerGame,
    actor: Actor? = null,
    /*
        Heuristic Function should take the form of (Start, Goal, Game, Actor?). The default only looks for
        the shortest passable distance.
     */
    heuristicFunction: (Coordinates, Coordinates, WizardTowerGame) -> Int = { node, goal, game ->
        val maybeTile = game.tilemap
            .getTileOrNull(node)

        if (maybeTile == null)
            scoreDefault
        else if (!maybeTile.isPassable)
            scoreDefault
        else
            node.chebyshevDistance(goal)
    },
) {
    /*
        Algorithm pseudocode courtesy of Wikipedia:
        https://en.wikipedia.org/wiki/A*_search_algorithm

        Tips on A* performance courtesy of Reddit:
        https://www.reddit.com/r/roguelikedev/comments/59u44j/warning_a_and_manhattan_distance/
     */

    // The eventual resulting path:
    var path: List<Coordinates>? = null

    init {
        var finalPath = listOf<Coordinates>()

        /**
         * Reconstructs the path from origin to target according to the pcode on Wikipedia.
         */
        fun reconstructPath(
            cameFrom: Map<Coordinates, Coordinates>,
            current: Coordinates
        ): List<Coordinates> {
            val totalPath = mutableListOf(current)
            var temp = current
            while (temp in cameFrom.keys) {
                temp = cameFrom[temp]!!
                totalPath.add(temp)
            }
            return totalPath.reversed()
        }

        if (waypoints.size < 2) error("Waypoint size < 2")

        val mutableWaypoints = waypoints.toMutableList()
        var currentWaypoint = mutableWaypoints.removeFirst()

        /**
         * This is the biggest difference I made from the Wikipedia pcode. All I did was add one more layer of
         * abstraction with waypoints. This function runs the A* pcode from wikipedia almost verbatim from one
         * waypoint to another, allowing for multi-waypoint paths to be broken up in to different "legs" of the
         * process.
         */
        fun iterateWaypoints(nextWaypoint: Coordinates): Boolean {
            val cameFrom = mutableMapOf<Coordinates, Coordinates>()

            val gScore = mutableMapOf<Coordinates, Int>()
            gScore[currentWaypoint] = 0

            val fScore = mutableMapOf<Coordinates, Int>()

            val openSet = PriorityQueue { a: Coordinates, b: Coordinates ->
                val fScoreA = fScore.getOrElse(a) { scoreDefault }
                val fScoreB = fScore.getOrElse(b) { scoreDefault }

                if (fScoreA < fScoreB) -1
                else if (fScoreA > fScoreB) 1
                else 0
            }
            openSet.add(currentWaypoint)

            while (!openSet.isEmpty()) {
                val currentNode = openSet.remove()

                if (currentNode.matches(nextWaypoint)) {
                    finalPath = finalPath
                        .plus(reconstructPath(cameFrom, currentNode))
                    return true
                }

                val gScoreCurrent = gScore.getOrElse(currentNode) { scoreDefault }

                val bounds = game.tilemap.bounds()
                currentNode.neighbors(bounds).forEach { node ->
                    val gScoreNode = gScore.getOrElse(node) { scoreDefault }
                    val tentativeGScore = gScoreCurrent + currentNode.chebyshevDistance(node)

                    if (tentativeGScore < gScoreNode) {
                        cameFrom[node] = currentNode
                        gScore[node] = tentativeGScore

                        val hScore = heuristicFunction(node, nextWaypoint, game)
                        fScore[node] = gScore[node]!! + hScore

                        if (node !in openSet && hScore < scoreDefault) {
                            openSet.add(node)
                        }
                    }
                }
            }
            return false
        }

        // Run A* paths from waypoint to waypoint:
        while (mutableWaypoints.isNotEmpty()) {
            mutableWaypoints.removeFirstOrNull()?.let { nextWaypoint ->
                if (iterateWaypoints(nextWaypoint))
                    currentWaypoint = nextWaypoint
            }
        }

        // Set the final path:
        path = finalPath
    }

    /**
     * Direct is for when there are no waypoints and it is just point A to point B.
     */
    class Direct(
        start: Coordinates,
        goal: Coordinates,
        game: WizardTowerGame,
        actor: Actor?,
    ) : AStarPath(
        waypoints = listOf(start, goal),
        game = game,
        actor = actor,
    )

    /**
     * DirectSequence is for when you want a series of waypoints to be navigated in order.
     */
    class DirectSequence(
        waypoints: List<Coordinates>,
        game: WizardTowerGame,
        actor: Actor?,
    ) : AStarPath(
        waypoints = waypoints,
        game = game,
        actor = actor,
    )
}