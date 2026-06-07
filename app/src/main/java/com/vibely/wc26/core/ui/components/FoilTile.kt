package com.vibely.wc26.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Wraps [StickerTile] with a foil/sheen animation for Section 1 specials.
 * The sheen sweeps diagonally across the tile in a continuous loop,
 * mimicking the holographic effect on real Panini foil stickers.
 *
 * @param slotLabel short top-left label (sticker ID)
 * @param displayName the player/team name
 * @param quantity 0..N — drives visual states
 * @param onClick tap handler
 * @param onLongClick optional long-press handler
 * @param onSwipeRight optional swipe-right handler
 * @param onSwipeLeft optional swipe-left handler
 */
@Composable
fun FoilTile(
    slotLabel: String,
    displayName: String,
    quantity: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "foil_sheen")
    val sheenOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "sheen_offset",
    )

    Box(modifier = modifier) {
        StickerTile(
            slotLabel = slotLabel,
            displayName = displayName,
            quantity = quantity,
            onClick = onClick,
            onLongClick = onLongClick,
            onSwipeRight = onSwipeRight,
            onSwipeLeft = onSwipeLeft,
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = 0.15f)
                    .drawBehind {
                        drawRect(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            androidx.compose.ui.graphics.Color.Transparent,
                                            androidx.compose.ui.graphics.Color.White,
                                            androidx.compose.ui.graphics.Color.Transparent,
                                        ),
                                    start = Offset(sheenOffset, 0f),
                                    end = Offset(sheenOffset + 100f, size.height),
                                ),
                        )
                    },
        )
    }
}
