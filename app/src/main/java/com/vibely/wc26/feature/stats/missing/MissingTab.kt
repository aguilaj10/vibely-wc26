package com.vibely.wc26.feature.stats.missing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.core.ui.components.EmptyState
import com.vibely.wc26.core.ui.export.rememberExportActions
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.usecase.MissingData

@Composable
fun MissingTab(
    modifier: Modifier = Modifier,
    viewModel: MissingViewModel = hiltViewModel(),
) {
    val data by viewModel.state.collectAsStateWithLifecycle()
    val actions = rememberExportActions(clipboardLabel = MissingClipboardLabel)
    val exportText = remember(data) { formatMissingExport(data) }

    if (data.sections.isEmpty()) {
        EmptyState(
            modifier = modifier,
            title = stringResource(R.string.stats_missing_empty_title),
            description = stringResource(R.string.stats_missing_empty_description),
            icon = Icons.Outlined.CheckCircle,
        )
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        ExportRow(
            totalCount = data.totalMissing,
            countLabel = stringResource(R.string.stats_missing_total_format, data.totalMissing),
            onCopy = { actions.copy(exportText) },
            onShare = { actions.share(exportText) },
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            data.sections.forEachIndexed { sectionIndex, section ->
                item(key = "section:$sectionIndex") {
                    SectionHeader(
                        label = sectionLabel(section),
                        count = section.stickers.size,
                    )
                }
                items(section.stickers, key = { "missing:${it.id}" }) { sticker ->
                    MissingRow(sticker = sticker)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun sectionLabel(section: com.vibely.wc26.domain.usecase.MissingSection): String =
    when {
        section.isSpecials -> stringResource(R.string.browse_specials_label)
        else -> section.teamName.orEmpty()
    }

@Composable
private fun ExportRow(
    totalCount: Int,
    countLabel: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = countLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp),
        )
        AssistChip(
            onClick = onCopy,
            label = { Text(stringResource(R.string.stats_action_copy)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(),
            enabled = totalCount > 0,
        )
        AssistChip(
            onClick = onShare,
            label = { Text(stringResource(R.string.stats_action_share)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(),
            enabled = totalCount > 0,
        )
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.stats_missing_section_count, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MissingRow(sticker: Sticker) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = sticker.id,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = sticker.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Comma-joined sticker ids, suitable for clipboard or share-sheet payload. */
private fun formatMissingExport(data: MissingData): String =
    data.sections.flatMap { it.stickers }.joinToString(", ") { it.id }

private const val MissingClipboardLabel = "WC26 missing"
