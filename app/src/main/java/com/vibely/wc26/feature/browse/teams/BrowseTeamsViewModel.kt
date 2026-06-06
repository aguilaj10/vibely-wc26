package com.vibely.wc26.feature.browse.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.ProgressSummary
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.usecase.GetOverallStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BrowseTeamsViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val getOverallStats: GetOverallStatsUseCase,
) : ViewModel() {

    private val groupLetter = MutableStateFlow<String?>(null)

    fun setGroup(letter: String) {
        groupLetter.value = letter
    }

    val state: StateFlow<BrowseTeamsUiState> = groupLetter
        .filterNotNull()
        .flatMapLatest { letter ->
            val catalogFlow = flow { emit(catalogRepository.load()) }
            combine(catalogFlow, getOverallStats()) { catalog, stats ->
                val teams = catalog.teams
                    .filter { it.group == letter }
                    .sortedBy { it.seed }
                BrowseTeamsUiState(
                    groupLetter = letter,
                    teams = teams.map { team ->
                        TeamRow(
                            team = team,
                            summary = stats.perTeam[team.code]
                                ?: ProgressSummary(0, 0, 0, 0),
                        )
                    },
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BrowseTeamsUiState.Empty,
        )
}

data class TeamRow(
    val team: Team,
    val summary: ProgressSummary,
)

data class BrowseTeamsUiState(
    val groupLetter: String,
    val teams: List<TeamRow>,
) {
    companion object {
        val Empty = BrowseTeamsUiState(groupLetter = "", teams = emptyList())
    }
}
