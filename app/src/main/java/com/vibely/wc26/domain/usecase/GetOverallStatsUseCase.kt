package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.CollectionStats
import com.vibely.wc26.domain.model.ProgressSummary
import com.vibely.wc26.domain.model.StickerCatalog
import com.vibely.wc26.domain.model.StickerType
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Emits a fresh [CollectionStats] every time the user's ownership changes.
 * Catalog is loaded once and held by the repository's in-memory cache.
 */
class GetOverallStatsUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
) {
    operator fun invoke(): Flow<CollectionStats> = flow {
        val catalog = catalogRepository.load()
        emitAll(
            ownershipRepository.observeAll().map { ownership -> compute(catalog, ownership) }
        )
    }

    private fun compute(catalog: StickerCatalog, ownership: Map<String, Int>): CollectionStats {
        val all = ArrayList<Int>(catalog.stickers.size)
        val byGroup = HashMap<String, ArrayList<Int>>()
        val byTeam = HashMap<String, ArrayList<Int>>()
        val specials = ArrayList<Int>()

        for (sticker in catalog.stickers) {
            val q = ownership[sticker.id] ?: 0
            all.add(q)
            if (sticker.type == StickerType.SPECIAL) {
                specials.add(q)
            } else {
                sticker.group?.let { byGroup.getOrPut(it) { ArrayList() }.add(q) }
                sticker.teamCode?.let { byTeam.getOrPut(it) { ArrayList() }.add(q) }
            }
        }

        return CollectionStats(
            overall = summarize(all),
            perGroup = byGroup.mapValues { summarize(it.value) },
            perTeam = byTeam.mapValues { summarize(it.value) },
            specials = summarize(specials),
        )
    }

    private fun summarize(quantities: List<Int>): ProgressSummary {
        var owned = 0
        var dupStickers = 0
        var totalDups = 0
        for (q in quantities) {
            if (q >= 1) owned++
            if (q >= 2) {
                dupStickers++
                totalDups += q - 1
            }
        }
        return ProgressSummary(
            owned = owned,
            total = quantities.size,
            duplicateStickers = dupStickers,
            totalDuplicates = totalDups,
        )
    }
}
