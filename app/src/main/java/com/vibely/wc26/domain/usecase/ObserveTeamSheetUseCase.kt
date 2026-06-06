package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.ProgressSummary
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.ownership.OwnershipRepository
import com.vibely.wc26.domain.prefs.PlayerSort
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Emits a fresh [TeamSheetData] every time ownership, sort preference, or the
 * requested team changes. Pure derivation — no side effects.
 *
 * Returns `null` from [TeamSheetData.team] when the team code is unknown, so
 * the caller can render an empty/error state without crashing.
 */
class ObserveTeamSheetUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
    private val preferences: UserPreferencesRepository,
) {
    operator fun invoke(teamCode: String): Flow<TeamSheetData> {
        val catalogFlow = flow { emit(catalogRepository.load()) }
        return combine(
            catalogFlow,
            ownershipRepository.observeAll(),
            preferences.preferences.map { it.playerSort },
        ) { catalog, ownership, sort ->
            val team = catalog.teams.firstOrNull { it.code == teamCode }
            if (team == null) {
                TeamSheetData.Empty
            } else {
                buildData(team, catalog.stickers, ownership, sort)
            }
        }
    }

    private fun buildData(
        team: Team,
        allStickers: List<Sticker>,
        ownership: Map<String, Int>,
        sort: PlayerSort,
    ): TeamSheetData {
        val rows = allStickers
            .asSequence()
            .filter { it.teamCode == team.code }
            .map { sticker -> TeamStickerRow(sticker = sticker, quantity = ownership[sticker.id] ?: 0) }
            .let { seq ->
                when (sort) {
                    PlayerSort.Slot -> seq.sortedBy { it.sticker.slotIndex ?: 0 }
                    PlayerSort.Alphabetical -> seq.sortedBy { it.sticker.displayName }
                }
            }
            .toList()
        val owned = rows.count { it.quantity >= 1 }
        val dupes = rows.sumOf { (it.quantity - 1).coerceAtLeast(0) }
        val dupStickers = rows.count { it.quantity >= 2 }
        return TeamSheetData(
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

data class TeamStickerRow(
    val sticker: Sticker,
    val quantity: Int,
)

data class TeamSheetData(
    val team: Team?,
    val rows: List<TeamStickerRow>,
    val summary: ProgressSummary,
    val sort: PlayerSort,
) {
    companion object {
        val Empty = TeamSheetData(
            team = null,
            rows = emptyList(),
            summary = ProgressSummary(0, 0, 0, 0),
            sort = PlayerSort.Slot,
        )
    }
}
