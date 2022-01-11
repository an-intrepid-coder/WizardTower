package inputoutput

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class LabeledTextDataBundle(
    val label: String,
    val value: String,
    val color: Color
)

/**
 * Displays the contents of a LabeledTextDataBundle, formatted in monospace.
 */
@Composable
fun LabeledText(
    dataBundle: LabeledTextDataBundle
) {
    Text(
        text = "${dataBundle.label}: ${dataBundle.value}",
        color = dataBundle.color,
        fontFamily = FontFamily.Monospace,
    )
    Spacer(Modifier.height(displaySpacing))
}