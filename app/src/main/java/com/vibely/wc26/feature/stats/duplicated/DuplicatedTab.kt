package com.vibely.wc26.feature.stats.duplicated

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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.core.ui.components.EmptyState
import com.vibely.wc26.core.ui.export.rememberExportActions
import com.vibely.wc26.core.ui.theme.OwnedAccentDark
import com.vibely.wc26.core.ui.theme.OwnedAccentLight
import com.vibely.wc26.domain.usecase.DuplicatedRow
import com.vibely.wc26.domain.usecase.DuplicatedSort

@Composable
fun DuplicatedTab(
    modifier: Modifier = Modifier,
    viewModel: DuplicatedViewModel = hiltViewModel(),
) {
    val data by viewModel.state.collectAsStateWithLifecycle()
    val actions = rememberExportActions(clipboardLabel = DuplicatedClipboardLabel)
    val accent = if (androidx.compose.foundation.isSystemInDarkTheme()) OwnedAccentDark else OwnedAccentLight

    if (data.rows.isEmpty()) {
        EmptyState(
            modifier = modifier,
            title = stringResource(R.string.stats_duplicated_empty_title),
            description = stringResource(R.string.stats_duplicated_empty_description),
        )
        return
    }

    val exportText = formatExport(data.rows)

    Column(modifier = modifier.fillMaxSize()) {
        ExportRow(
            sort = data.sort,
            onSetSort = viewModel::setSort,
            onCopy = { actions.copy(exportText) },
            onShare = { actions.share(exportText) },
        )
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(data.rows, key = { "dup:${it.sticker.id}" }) { row ->
                DuplicatedRowItem(row = row, badgeColor = accent)
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
            item(key = "__footer__") {
                Footer(totalDuplicates = data.totalDuplicates, stickerCount = data.stickerCount)
            }
        }
    }
}

/** Format each row as "MEX02 ×2 (1 dupe)" then comma-join. Locale-agnostic. */
@Composable
private fun formatExport(rows: List<DuplicatedRow>): String {
    val template = stringResource(R.string.stats_duplicated_export_row_format)
    val dupeWord = rows.map { row ->
        pluralStringResource(R.plurals.stats_duplicated_dupe_word, row.dupeCount, row.dupeCount)
    }
    return rows.mapIndexed { index, row ->
        template
            .replace("%1\$s", row.sticker.id)
            .replace("%2\$d", row.quantity.toString())
            .replace("%3\$s", dupeWord[index])
    }.joinToString(", ")
}

@Composable
private fun ExportRow(
    sort: DuplicatedSort,
    onSetSort: (DuplicatedSort) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = { sortMenuOpen = true },
            label = { Text(stringResource(sortLabelRes(sort))) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp),
                )
            },
            modifier = Modifier.weight(1f, fill = false),
        )
        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
            SortMenuItem(
                label = stringResource(R.string.stats_duplicated_sort_most_dupes),
                selected = sort == DuplicatedSort.MostDuplicated,
                onClick = {
                    onSetSort(DuplicatedSort.MostDuplicated)
                    sortMenuOpen = false
                },
            )
            SortMenuItem(
                label = stringResource(R.string.stats_duplicated_sort_by_team),
                selected = sort == DuplicatedSort.ByTeam,
                onClick = {
                    onSetSort(DuplicatedSort.ByTeam)
                    sortMenuOpen = false
                },
            )
        }
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
        )
    }
}

@Composable
private fun SortMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        leadingIcon = { RadioButton(selected = selected, onClick = onClick) },
        onClick = onClick,
    )
}

private fun sortLabelRes(sort: DuplicatedSort): Int = when (sort) {
    DuplicatedSort.MostDuplicated -> R.string.stats_duplicated_sort_most_dupes
    DuplicatedSort.ByTeam -> R.string.stats_duplicated_sort_by_team
}

@Composable
private fun DuplicatedRowItem(row: DuplicatedRow, badgeColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.sticker.id,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.sticker.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subtitle = row.team?.name ?: row.sticker.section
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "×${row.quantity}",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
            color = badgeColor,
        )
        Text(
            text = pluralStringResource(R.plurals.stats_duplicated_dupes_label, row.dupeCount, row.dupeCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Footer(totalDuplicates: Int, stickerCount: Int) {
    val stickerWord = pluralStringResource(
        R.plurals.stats_duplicated_footer_stickers,
        stickerCount,
        stickerCount,
    )
    Text(
        text = stringResource(R.string.stats_duplicated_footer, totalDuplicates, stickerWord),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    )
}

private const val DuplicatedClipboardLabel = "WC26 duplicates"
