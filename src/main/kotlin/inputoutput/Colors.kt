package inputoutput

import androidx.compose.ui.graphics.Color
import game.Faction

val DarkGray = Color(30, 30, 30)
val White = Color(255, 255,255)
val Black = Color(0, 0, 0)
val GoGreen = Color(0, 255, 0)
val AlertRed = Color(255, 0, 0)
val CautionYellow = Color(255, 255, 0)
val BrightBlue = Color(10, 160, 255)
val BrightPurple = Color(255, 60, 255)
val LightGray = Color(220, 220, 220)
val Dirt = Color(153, 112, 0)

val PlayerColor = GoGreen
val NeutralColor = BrightBlue
val HostileColor = AlertRed
val NoFactionColor = White

val factionColors = mapOf(
    Faction.PLAYER to PlayerColor,
    Faction.NEUTRAL to NeutralColor,
    Faction.HOSTILE to HostileColor,
    null to NoFactionColor,
)
/**
 * Returns a random color within a mid-low range. One neat side effect of this is that, while waiting consecutive
 * turns, the surrounding fog of war appears to "shimmer".
 */
fun randomFoggyColor(): Color {
    val fogRange = 80..120
    return Color(fogRange.random(), fogRange.random(), fogRange.random())
}