package com.vibely.wc26.feature.stats.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.CollectionStats
import com.vibely.wc26.domain.model.ProgressSummary
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.usecase.GetOverallStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProgressViewModel @Inject constructor(
    getOverallStats: GetOverallStatsUseCase,
    catalogRepository: CatalogRepository,
) : ViewModel() {

    val state: StateFlow<ProgressUiState> = combine(
        getOverallStats(),
        flow { emit(catalogRepository.load()) },
    ) { stats, catalog ->
        ProgressUiState(
            stats = stats,
            groups = catalog.teams
                .map { it.group }
                .distinct()
                .sorted()
                .map { letter -> GroupRow(letter = letter, summary = stats.perGroup[letter]) },
            teams = catalog.teams
                .sortedWith(compareBy({ it.group }, { it.seed }))
                .map { team -> TeamRow(team = team, summary = stats.perTeam[team.code]) },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        ProgressUiState.Empty,
    )

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

data class GroupRow(val letter: String, val summary: ProgressSummary?)

data class TeamRow(val team: Team, val summary: ProgressSummary?)

data class ProgressUiState(
    val stats: CollectionStats?,
    val groups: List<GroupRow>,
    val teams: List<TeamRow>,
) {
    companion object {
        val Empty = ProgressUiState(stats = null, groups = emptyList(), teams = emptyList())
    }
}
