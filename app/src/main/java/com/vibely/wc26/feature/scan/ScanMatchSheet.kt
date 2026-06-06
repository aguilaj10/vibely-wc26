package com.vibely.wc26.feature.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vibely.wc26.R
import com.vibely.wc26.domain.model.StickerMatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanMatchSheet(
    matched: ScanUiState.Matched,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSave: () -> Unit,
    onSelectAlternative: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        MatchContent(
            matched = matched,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            onSave = onSave,
            onSelectAlternative = onSelectAlternative,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanNoMatchSheet(
    onTryAgain: () -> Unit,
    onSearchManually: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(48.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.scan_no_match_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.scan_no_match_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onSearchManually,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.scan_no_match_search)) }
                Button(
                    onClick = onTryAgain,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.scan_no_match_try_again)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MatchContent(
    matched: ScanUiState.Matched,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSave: () -> Unit,
    onSelectAlternative: (String) -> Unit,
) {
    var altsExpanded by remember { mutableStateOf(false) }
    val best = matched.best
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.scan_matched_title),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = headerLine(best),
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = best.sticker.displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        QuantityStepper(
            quantity = matched.pendingQuantity,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
        )
        if (matched.ownedQuantity > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.scan_owned_baseline, matched.ownedQuantity),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.scan_save_and_continue)) }

        if (matched.alternatives.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { altsExpanded = !altsExpanded }) {
                Text(
                    text = if (altsExpanded) {
                        stringResource(R.string.scan_alternatives_hide)
                    } else {
                        stringResource(R.string.scan_alternatives_show)
                    },
                )
            }
            AnimatedVisibility(visible = altsExpanded) {
                AlternativesList(
                    alternatives = matched.alternatives,
                    onClick = onSelectAlternative,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        StepperButton(
            icon = Icons.Outlined.Remove,
            contentDescription = stringResource(R.string.sticker_detail_decrement),
            enabled = quantity > 0,
            onClick = onDecrement,
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        StepperButton(
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.sticker_detail_increment),
            enabled = true,
            onClick = onIncrement,
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
private fun AlternativesList(
    alternatives: List<StickerMatch>,
    onClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        alternatives.forEach { alt ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(alt.sticker.id) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = alt.sticker.id,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = alt.sticker.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = alt.team?.code.orEmpty(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun headerLine(match: StickerMatch): String {
    val team = match.team
    return when {
        team != null -> stringResource(
            R.string.sticker_detail_header_team,
            match.sticker.id,
            team.name,
            team.group,
        )
        match.sticker.section != null -> stringResource(
            R.string.sticker_detail_header_special,
            match.sticker.id,
            match.sticker.section,
        )
        else -> match.sticker.id
    }
}
