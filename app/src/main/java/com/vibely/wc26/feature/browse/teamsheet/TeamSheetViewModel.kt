package com.vibely.wc26.feature.browse.teamsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.prefs.PlayerSort
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import com.vibely.wc26.domain.usecase.ObserveTeamSheetUseCase
import com.vibely.wc26.domain.usecase.TeamSheetData
import com.vibely.wc26.domain.usecase.TeamStickerRow
import com.vibely.wc26.domain.usecase.UpdateStickerQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamSheetViewModel @Inject constructor(
    private val observeTeamSheet: ObserveTeamSheetUseCase,
    private val updateQuantity: UpdateStickerQuantityUseCase,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    private val teamCode = MutableStateFlow<String?>(null)
    private val selectedStickerId = MutableStateFlow<String?>(null)
    private val celebrationEvents = Channel<TeamCelebration>(capacity = Channel.CONFLATED)

    val celebrations: Flow<TeamCelebration> = celebrationEvents.receiveAsFlow()

    fun setTeam(code: String) {
        teamCode.value = code
    }

    fun setSort(sort: PlayerSort) {
        viewModelScope.launch { preferences.setPlayerSort(sort) }
    }

    fun openSheet(stickerId: String) {
        selectedStickerId.value = stickerId
    }

    fun closeSheet() {
        selectedStickerId.value = null
    }

    fun increment(stickerId: String) {
        viewModelScope.launch { updateQuantity.adjust(stickerId, +1) }
    }

    fun decrement(stickerId: String) {
        viewModelScope.launch { updateQuantity.adjust(stickerId, -1) }
    }

    fun setQuantity(stickerId: String, quantity: Int) {
        viewModelScope.launch { updateQuantity.set(stickerId, quantity) }
    }

    /** Called by the screen once the overlay finishes; persist so we don't re-celebrate. */
    fun acknowledgeCelebration(teamCode: String) {
        viewModelScope.launch { preferences.markTeamCelebrated(teamCode) }
    }

    private val teamSheet: Flow<TeamSheetData> = teamCode
        .filterNotNull()
        .flatMapLatest { code -> observeTeamSheet(code) }

    val state: StateFlow<TeamSheetUiState> = combine(
        teamSheet,
        selectedStickerId,
    ) { data, selectedId ->
        TeamSheetUiState(
            team = data.team,
            rows = data.rows,
            sort = data.sort,
            ownedCount = data.summary.owned,
            totalCount = data.summary.total,
            selectedSticker = selectedId?.let { id ->
                data.rows.firstOrNull { it.sticker.id == id }
            },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
        TeamSheetUiState.Empty,
    )

    init {
        observeCelebrationTransitions()
    }

    /**
     * Watches `teamSheet` for the <100% → 100% transition. Runs as a dedicated
     * collector so the pure mapping in [state] stays side-effect free, and so
     * re-emissions for other reasons (sort toggle, sheet open) don't re-fire.
     */
    private fun observeCelebrationTransitions() {
        val completion: Flow<TeamCompletion?> = teamSheet
            .map { data ->
                val team = data.team ?: return@map null
                TeamCompletion(team = team, isComplete = data.summary.isComplete)
            }
            .distinctUntilChanged()

        viewModelScope.launch {
            combine(
                completion,
                preferences.preferences.map { it.celebratedTeams },
            ) { snapshot, celebrated -> snapshot to celebrated }
                .scan(InitialTransition) { acc, (snapshot, celebrated) ->
                    val prevComplete = acc.lastComplete?.takeIf { it.team.code == snapshot?.team?.code }
                    val crossed = snapshot != null &&
                        snapshot.isComplete &&
                        prevComplete?.isComplete != true &&
                        snapshot.team.code !in celebrated
                    TransitionState(lastComplete = snapshot, fireFor = if (crossed) snapshot.team else null)
                }
                .onEach { transition ->
                    val team = transition.fireFor ?: return@onEach
                    celebrationEvents.trySend(TeamCelebration(teamCode = team.code, teamName = team.name))
                }
                .collect()
        }
    }

    private data class TeamCompletion(val team: Team, val isComplete: Boolean)
    private data class TransitionState(val lastComplete: TeamCompletion?, val fireFor: Team?)

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
        val InitialTransition = TransitionState(lastComplete = null, fireFor = null)
    }
}

data class TeamCelebration(val teamCode: String, val teamName: String)

data class TeamSheetUiState(
    val team: Team?,
    val rows: List<TeamStickerRow>,
    val sort: PlayerSort,
    val ownedCount: Int,
    val totalCount: Int,
    val selectedSticker: TeamStickerRow? = null,
) {
    companion object {
        val Empty = TeamSheetUiState(
            team = null,
            rows = emptyList(),
            sort = PlayerSort.Slot,
            ownedCount = 0,
            totalCount = 0,
            selectedSticker = null,
        )
    }
}
