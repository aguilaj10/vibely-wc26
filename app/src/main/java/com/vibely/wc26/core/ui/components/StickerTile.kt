package com.vibely.wc26.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
import kotlin.math.abs

/**
 * One sticker slot in the 4×5 team sheet grid (and reused in search/scan match preview).
 *
 * Visual states:
 *   - quantity == 0 → dashed outline, dimmed label   (album-faithful placeholder)
 *   - quantity == 1 → 1dp solid outline, normal label (stuck down)
 *   - quantity ≥ 2  → 1dp solid outline + gold ×N badge (duplicated)
 *
 * Gestures (DESIGN.md §6):
 *   - tap                → [onClick] (typically opens detail sheet)
 *   - long-press         → [onLongClick] (quick +1)
 *   - swipe right (+1)   → [onSwipeRight] with single haptic tick
 *   - swipe left (−1)    → [onSwipeLeft] with single haptic tick
 *
 * @param slotLabel short top-left label, typically the slot number (e.g. "02")
 * @param displayName the longer label shown in the center (e.g. "Malagón")
 * @param quantity 0..N — drives all visual states
 * @param onClick required tap handler — opens sticker detail
 * @param onLongClick optional long-press — quick +1
 * @param onSwipeRight optional horizontal-drag-right gesture — +1
 * @param onSwipeLeft optional horizontal-drag-left gesture — −1
 */
@Composable
fun StickerTile(
    slotLabel: String,
    displayName: String,
    quantity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
) {
    val owned = isOwned(quantity)
    val accent = if (isSystemInDarkTheme()) OwnedAccentDark else OwnedAccentLight
    val outline = MaterialTheme.colorScheme.outline
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val shape = RoundedCornerShape(10.dp)
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { SwipeThresholdDp.dp.toPx() }

    val tileDescription =
        buildString {
            append(displayName)
            append(", ")
            when {
                quantity == 0 -> {
                    append("missing")
                }

                isDuplicated(quantity) -> {
                    append("owned, ")
                    append(duplicatesOf(quantity) + 1)
                    append(" copies")
                }

                else -> {
                    append("owned")
                }
            }
        }
    val baseModifier =
        modifier
            .aspectRatio(0.78f)
            .clip(shape)
            .semantics {
                role = Role.Button
                contentDescription = tileDescription
            }.combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .swipeOnce(
                thresholdPx = swipeThresholdPx,
                onSwipeRight =
                    onSwipeRight?.let {
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            it()
                        }
                    },
                onSwipeLeft =
                    onSwipeLeft?.let {
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            it()
                        }
                    },
            )

    val containerModifier =
        if (owned) {
            baseModifier
                .background(MaterialTheme.colorScheme.surface)
                .drawBehind {
                    drawRoundRect(
                        color = outlineVariant,
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                    )
                }
        } else {
            // empty slot: dashed outline, faithful to the printed album
            baseModifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .drawBehind {
                    drawRoundRect(
                        color = outline,
                        style =
                            Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                            ),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                    )
                }
        }

    Box(modifier = containerModifier) {
        Column(
            modifier =
                Modifier
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
                    color =
                        if (owned) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
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
                color =
                    if (owned) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 4.dp),
            )
        }
    }
}

/**
 * Detect a horizontal swipe and fire at most once per drag gesture.
 * Accumulates horizontal drag; when the magnitude crosses [thresholdPx],
 * fires the corresponding callback and ignores the rest of that drag.
 */
private fun Modifier.swipeOnce(
    thresholdPx: Float,
    onSwipeRight: (() -> Unit)?,
    onSwipeLeft: (() -> Unit)?,
): Modifier {
    if (onSwipeRight == null && onSwipeLeft == null) return this
    return this.pointerInput(thresholdPx, onSwipeRight, onSwipeLeft) {
        var totalDrag = 0f
        var fired = false
        detectHorizontalDragGestures(
            onDragStart = {
                totalDrag = 0f
                fired = false
            },
            onDragEnd = {
                totalDrag = 0f
                fired = false
            },
            onDragCancel = {
                totalDrag = 0f
                fired = false
            },
            onHorizontalDrag = { _, dragAmount ->
                if (fired) return@detectHorizontalDragGestures
                totalDrag += dragAmount
                if (abs(totalDrag) >= thresholdPx) {
                    fired = true
                    if (totalDrag > 0) onSwipeRight?.invoke() else onSwipeLeft?.invoke()
                }
            },
        )
    }
}

private const val SwipeThresholdDp = 32

@Preview(showBackground = true)
@Composable
private fun StickerTilePreview() {
    PaniniWC26Theme {
        Row(
            modifier =
                Modifier
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
