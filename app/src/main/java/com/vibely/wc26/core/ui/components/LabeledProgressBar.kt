package com.vibely.wc26.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vibely.wc26.core.ui.theme.PaniniWC26Theme

/**
 * Horizontal progress bar with a leading text label and an optional trailing % label.
 * Used on Home (overall progress), Browse rows (per group / per team), and Stats.
 *
 * @param progress 0f..1f
 * @param label leading text, e.g. "714 / 994"
 * @param trailing optional trailing text, typically the percentage
 * @param color bar color; defaults to MaterialTheme.colorScheme.primary
 */
@Composable
fun LabeledProgressBar(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "progress",
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledProgressBarPreview() {
    PaniniWC26Theme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LabeledProgressBar(progress = 0.617f, label = "714 / 994", trailing = "61.7%")
            LabeledProgressBar(progress = 0.25f, label = "Group A", trailing = "5 / 20")
            LabeledProgressBar(progress = 1f, label = "Mexico", trailing = "20 / 20")
        }
    }
}
