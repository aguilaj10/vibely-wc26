package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Emits the user's duplicate inventory: every sticker with quantity ≥ 2, with
 * `dupeCount = quantity - 1` (the count available to trade). [sort] controls
 * the ordering — `MostDuplicated` is the default; `ByTeam` mirrors album order
 * so a collector can scan their pile by section.
 */
class ObserveDuplicatedStickersUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
) {
    operator fun invoke(sort: Flow<DuplicatedSort>): Flow<DuplicatedData> = combine(
        flow { emit(catalogRepository.load()) },
        ownershipRepository.observeAll(),
        sort,
    ) { catalog, ownership, order ->
        val teamsByCode = catalog.teams.associateBy { it.code }
        val rows = catalog.stickers
            .mapNotNull { sticker ->
                val qty = ownership[sticker.id] ?: 0
                if (qty < 2) null else DuplicatedRow(
                    sticker = sticker,
                    team = sticker.teamCode?.let(teamsByCode::get),
                    quantity = qty,
                    dupeCount = qty - 1,
                )
            }

        if (rows.isEmpty()) return@combine DuplicatedData.Empty

        val ordered = when (order) {
            DuplicatedSort.MostDuplicated -> rows.sortedWith(
                compareByDescending<DuplicatedRow> { it.dupeCount }
                    .thenBy { it.team?.group ?: SPECIALS_SORT_KEY }
                    .thenBy { it.team?.seed ?: 0 }
                    .thenBy { it.sticker.slotIndex ?: 0 },
            )
            DuplicatedSort.ByTeam -> rows.sortedWith(
                compareBy<DuplicatedRow> { it.team?.group ?: SPECIALS_SORT_KEY }
                    .thenBy { it.team?.seed ?: 0 }
                    .thenBy { it.sticker.slotIndex ?: 0 },
            )
        }

        DuplicatedData(
            rows = ordered,
            sort = order,
            totalDuplicates = rows.sumOf { it.dupeCount },
            stickerCount = rows.size,
        )
    }

    private companion object {
        /** Sorts specials after every group letter (Z + 1). */
        const val SPECIALS_SORT_KEY = "ZZ"
    }
}

enum class DuplicatedSort { MostDuplicated, ByTeam }

data class DuplicatedRow(
    val sticker: Sticker,
    val team: Team?,
    val quantity: Int,
    val dupeCount: Int,
)

data class DuplicatedData(
    val rows: List<DuplicatedRow>,
    val sort: DuplicatedSort,
    val totalDuplicates: Int,
    val stickerCount: Int,
) {
    companion object {
        val Empty = DuplicatedData(
            rows = emptyList(),
            sort = DuplicatedSort.MostDuplicated,
            totalDuplicates = 0,
            stickerCount = 0,
        )
    }
}
