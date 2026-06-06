package com.vibely.wc26.feature.browse.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.model.ProgressSummary
import com.vibely.wc26.domain.usecase.GetOverallStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BrowseGroupsViewModel @Inject constructor(
    getOverallStats: GetOverallStatsUseCase,
) : ViewModel() {

    val state: StateFlow<BrowseGroupsUiState> = getOverallStats()
        .map { stats ->
            BrowseGroupsUiState(
                groups = stats.perGroup.entries
                    .sortedBy { it.key }
                    .map { (letter, summary) -> GroupRow(letter, summary) },
                specials = stats.specials,
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BrowseGroupsUiState.Empty,
        )
}

data class GroupRow(
    val letter: String,
    val summary: ProgressSummary,
)

data class BrowseGroupsUiState(
    val groups: List<GroupRow>,
    val specials: ProgressSummary,
) {
    companion object {
        val Empty = BrowseGroupsUiState(
            groups = emptyList(),
            specials = ProgressSummary(0, 0, 0, 0),
        )
    }
}
