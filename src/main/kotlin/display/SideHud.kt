package display

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The Side HUD is for whatever misc. information. It is scrollable, and they can be placed side-by-side.
 */
@Composable
fun SideHud(
    dataList: List<LabeledTextDataBundle>
) {
    LazyColumn(
        modifier = Modifier
            .background(DarkGray),
    ) {
        dataList.forEach { bundle ->
            item {
                LabeledText(bundle)
            }
        }
    }
}