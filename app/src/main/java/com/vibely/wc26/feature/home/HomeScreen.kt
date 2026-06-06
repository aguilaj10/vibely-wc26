package com.vibely.wc26.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.core.ui.components.HubCard
import com.vibely.wc26.core.ui.components.LabeledProgressBar
import com.vibely.wc26.core.ui.format.countLabel
import com.vibely.wc26.core.ui.format.percentLabel
import com.vibely.wc26.domain.model.Sticker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onBrowse: () -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onStats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
    ) { inner ->
        HomeContent(
            ui = ui,
            onBrowse = onBrowse,
            onSearch = onSearch,
            onScan = onScan,
            onStats = onStats,
            modifier = Modifier.padding(inner),
        )
    }
}

@Composable
private fun HomeContent(
    ui: HomeUiState,
    onBrowse: () -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val stats = ui.stats
        if (stats != null) {
            val overall = stats.overall
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.home_overall_progress),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                LabeledProgressBar(
                    progress = overall.progress,
                    label = overall.countLabel,
                    trailing = overall.percentLabel,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HubCard(
                label = stringResource(R.string.home_card_browse),
                icon = Icons.Outlined.FolderOpen,
                onClick = onBrowse,
                modifier = Modifier.weight(1f),
            )
            HubCard(
                label = stringResource(R.string.home_card_search),
                icon = Icons.Outlined.Search,
                onClick = onSearch,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HubCard(
                label = stringResource(R.string.home_card_scan),
                icon = Icons.Outlined.PhotoCamera,
                onClick = onScan,
                modifier = Modifier.weight(1f),
            )
            HubCard(
                label = stringResource(R.string.home_card_stats),
                icon = Icons.Outlined.QueryStats,
                onClick = onStats,
                modifier = Modifier.weight(1f),
            )
        }

        if (stats != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.home_stats_inline,
                        stats.overall.missing,
                        stats.overall.totalDuplicates,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LastAddedLine(sticker = ui.lastAdded)
            }
        }
    }
}

@Composable
private fun LastAddedLine(sticker: Sticker?) {
    if (sticker == null) return
    Text(
        text = stringResource(R.string.home_last_added, sticker.id, sticker.displayName),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
