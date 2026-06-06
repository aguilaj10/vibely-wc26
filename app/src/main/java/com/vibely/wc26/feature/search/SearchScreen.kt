package com.vibely.wc26.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.core.ui.components.EmptyState
import com.vibely.wc26.core.ui.theme.OwnedAccentDark
import com.vibely.wc26.core.ui.theme.OwnedAccentLight
import com.vibely.wc26.core.util.duplicatesOf
import com.vibely.wc26.core.util.isDuplicated
import com.vibely.wc26.core.util.isOwned
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.usecase.SearchHit
import com.vibely.wc26.domain.usecase.SearchResultsData
import com.vibely.wc26.feature.stickerdetail.StickerDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.browse_back_content_desc),
                        )
                    }
                },
            )
        },
    ) { inner ->
        Column(modifier = Modifier.padding(inner).fillMaxSize()) {
            SearchField(
                query = state.query,
                onQueryChange = viewModel::setQuery,
            )
            HorizontalDivider()
            SearchResultsContent(
                results = state.results,
                onRowClick = viewModel::openSheet,
            )
        }

        val selected = state.selected
        if (selected != null) {
            StickerDetailSheet(
                sticker = selected.sticker,
                team = selected.team,
                quantity = selected.quantity,
                onIncrement = { viewModel.increment(selected.sticker.id) },
                onDecrement = { viewModel.decrement(selected.sticker.id) },
                onSetQuantity = { value -> viewModel.setQuantity(selected.sticker.id, value) },
                onDismiss = { viewModel.closeSheet() },
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.search_clear),
                    )
                }
            }
        },
        placeholder = { Text(stringResource(R.string.search_hint)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Search,
        ),
    )
}

@Composable
private fun SearchResultsContent(
    results: SearchResultsData,
    onRowClick: (String) -> Unit,
) {
    when (results) {
        SearchResultsData.Idle -> EmptyState(
            title = stringResource(R.string.search_idle_title),
            description = stringResource(R.string.search_idle_description),
            icon = Icons.Outlined.Search,
        )
        SearchResultsData.NoMatches -> EmptyState(
            title = stringResource(R.string.search_no_matches_title),
            description = stringResource(R.string.search_no_matches_description),
            icon = Icons.Outlined.SearchOff,
        )
        is SearchResultsData.Hits -> SearchHitsList(hits = results, onRowClick = onRowClick)
    }
}

@Composable
private fun SearchHitsList(
    hits: SearchResultsData.Hits,
    onRowClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "__total__") {
            Text(
                text = stringResource(R.string.search_total_format, hits.totalCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
        hits.sections.forEach { section ->
            item(key = "section:${section.label}") {
                SectionHeader(label = section.label)
            }
            items(items = section.rows, key = { "row:${it.sticker.id}" }) { hit ->
                SearchRow(hit = hit, onClick = { onRowClick(hit.sticker.id) })
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SearchRow(hit: SearchHit, onClick: () -> Unit) {
    val accent = if (isSystemInDarkTheme()) OwnedAccentDark else OwnedAccentLight
    // ListItem in M3 doesn't accept onClick — make the whole row clickable via Modifier.
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        headlineContent = {
            Text(
                text = hit.sticker.displayName,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = subtitle(hit.sticker, hit.team),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            QuantityBadge(quantity = hit.quantity, ownedAccent = accent)
        },
    )
}

@Composable
private fun QuantityBadge(quantity: Int, ownedAccent: Color) {
    when {
        !isOwned(quantity) -> Text(
            text = "·",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline,
        )
        isDuplicated(quantity) -> Text(
            text = "×${duplicatesOf(quantity) + 1}",
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
            color = ownedAccent,
        )
        else -> Text(
            text = "✓",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun subtitle(sticker: Sticker, team: Team?): String = buildString {
    append(sticker.id)
    if (team != null) {
        append(" · ")
        append(team.code)
    } else if (sticker.section != null) {
        append(" · ")
        append(sticker.section)
    }
}
