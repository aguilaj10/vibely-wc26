package com.vibely.wc26.feature.browse.teamsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibely.wc26.data.prefs.PlayerSort
import com.vibely.wc26.data.prefs.PrefsDataStore
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.ProgressSummary
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.ownership.OwnershipRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamSheetViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
    private val prefs: PrefsDataStore,
) : ViewModel() {

    private val teamCode = MutableStateFlow<String?>(null)

    fun setTeam(code: String) {
        teamCode.value = code
    }

    fun setSort(sort: PlayerSort) {
        viewModelScope.launch { prefs.setPlayerSort(sort) }
    }

    val state: StateFlow<TeamSheetUiState> = teamCode
        .filterNotNull()
        .flatMapLatest { code ->
            val catalogFlow = flow { emit(catalogRepository.load()) }
            combine(
                catalogFlow,
                ownershipRepository.observeAll(),
                prefs.preferences.map { it.playerSort },
            ) { catalog, ownership, sort ->
                val team = catalog.teams.firstOrNull { it.code == code }
                    ?: return@combine TeamSheetUiState.Empty
                val stickers = catalog.stickers.filter { it.teamCode == code }
                val rows = stickers.map { sticker ->
                    StickerRow(
                        sticker = sticker,
                        quantity = ownership[sticker.id] ?: 0,
                    )
                }.let { list ->
                    when (sort) {
                        PlayerSort.Slot -> list.sortedBy { it.sticker.slotIndex ?: 0 }
                        PlayerSort.Alphabetical -> list.sortedBy { it.sticker.displayName }
                    }
                }
                val owned = rows.count { it.quantity >= 1 }
                val dupes = rows.sumOf { (it.quantity - 1).coerceAtLeast(0) }
                val dupStickers = rows.count { it.quantity >= 2 }
                TeamSheetUiState(
                    team = team,
                    rows = rows,
                    summary = ProgressSummary(
                        owned = owned,
                        total = rows.size,
                        duplicateStickers = dupStickers,
                        totalDuplicates = dupes,
                    ),
                    sort = sort,
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TeamSheetUiState.Empty,
        )
}

data class StickerRow(
    val sticker: Sticker,
    val quantity: Int,
)

data class TeamSheetUiState(
    val team: Team?,
    val rows: List<StickerRow>,
    val summary: ProgressSummary,
    val sort: PlayerSort,
) {
    companion object {
        val Empty = TeamSheetUiState(
            team = null,
            rows = emptyList(),
            summary = ProgressSummary(0, 0, 0, 0),
            sort = PlayerSort.Slot,
        )
    }
}
