package com.vibely.wc26.feature.browse.teamsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibely.wc26.R
import com.vibely.wc26.core.ui.components.StickerTile
import com.vibely.wc26.data.prefs.PlayerSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSheetScreen(
    teamCode: String,
    onBack: () -> Unit,
    onStickerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamSheetViewModel = hiltViewModel(),
) {
    LaunchedEffect(teamCode) { viewModel.setTeam(teamCode) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val team = state.team
    var sortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (team != null) {
                        Text(
                            text = stringResource(
                                R.string.team_sheet_title_format,
                                team.name,
                                team.group,
                            ),
                        )
                    } else {
                        Text(text = "")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.browse_back_content_desc),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Sort,
                            contentDescription = stringResource(R.string.team_sheet_sort_label),
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false },
                    ) {
                        SortMenuItem(
                            label = stringResource(R.string.sort_by_slot),
                            selected = state.sort == PlayerSort.Slot,
                            onClick = {
                                viewModel.setSort(PlayerSort.Slot)
                                sortMenuOpen = false
                            },
                        )
                        SortMenuItem(
                            label = stringResource(R.string.sort_by_name),
                            selected = state.sort == PlayerSort.Alphabetical,
                            onClick = {
                                viewModel.setSort(PlayerSort.Alphabetical)
                                sortMenuOpen = false
                            },
                        )
                    }
                },
            )
        },
    ) { inner ->
        LazyVerticalGrid(
            modifier = Modifier.padding(inner),
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = state.rows, key = { it.sticker.id }) { row ->
                StickerTile(
                    slotLabel = row.sticker.slotIndex?.toString()?.padStart(2, '0') ?: "—",
                    displayName = shortName(row.sticker.displayName),
                    quantity = row.quantity,
                    onClick = { onStickerClick(row.sticker.id) },
                )
            }
        }
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

/** Last word of the name — fits the 4-col grid. "Luis Malagón" → "Malagón". */
private fun shortName(full: String): String =
    full.trim().substringAfterLast(' ').ifEmpty { full }
