package com.vibely.wc26.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vibely.wc26.core.ui.theme.OwnedAccentDark
import com.vibely.wc26.core.ui.theme.OwnedAccentLight
import com.vibely.wc26.core.ui.theme.PaniniWC26Theme
import com.vibely.wc26.core.util.duplicatesOf
import com.vibely.wc26.core.util.isDuplicated
import com.vibely.wc26.core.util.isOwned

/**
 * One sticker slot in the 4×5 team sheet grid (and reused in search/scan match preview).
 *
 * Visual states:
 *   - quantity == 0 → dashed outline, dimmed label   (album-faithful placeholder)
 *   - quantity == 1 → solid fill, normal label       (stuck down)
 *   - quantity ≥ 2  → solid fill + gold ×N badge     (duplicated)
 *
 * @param slotLabel short top-left label, typically the slot number (e.g. "02")
 * @param displayName the longer label shown in the center (e.g. "Malagón")
 * @param quantity 0..N — drives all visual states
 * @param onClick required tap handler — opens sticker detail
 * @param onLongClick optional long-press — quick +1 (DESIGN.md §6)
 */
@Composable
fun StickerTile(
    slotLabel: String,
    displayName: String,
    quantity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val owned = isOwned(quantity)
    val accent = if (isSystemInDarkTheme()) OwnedAccentDark else OwnedAccentLight
    val outline = MaterialTheme.colorScheme.outline
    val shape = RoundedCornerShape(10.dp)

    val baseModifier = modifier
        .aspectRatio(0.78f)
        .clip(shape)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)

    val containerModifier = if (owned) {
        baseModifier.background(MaterialTheme.colorScheme.surface)
    } else {
        // dashed outline for empty slot, faithful to the printed album
        baseModifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                )
            }
    }

    Box(modifier = containerModifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = slotLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = if (owned) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isDuplicated(quantity)) {
                    Text(
                        text = "×${duplicatesOf(quantity) + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = accent,
                    )
                }
            }
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                color = if (owned) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StickerTilePreview() {
    PaniniWC26Theme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StickerTile(
                slotLabel = "01",
                displayName = "Mexico",
                quantity = 0,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            StickerTile(
                slotLabel = "02",
                displayName = "Malagón",
                quantity = 1,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            StickerTile(
                slotLabel = "05",
                displayName = "Montes",
                quantity = 3,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
