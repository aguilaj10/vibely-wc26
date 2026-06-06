package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.StickerType
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Emits a fresh [SpecialsData] (grouped-by-section list of specials) on every
 * ownership change. Pure derivation.
 */
class ObserveSpecialsUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
) {
    operator fun invoke(): Flow<SpecialsData> = combine(
        flow { emit(catalogRepository.load()) },
        ownershipRepository.observeAll(),
    ) { catalog, ownership ->
        val specials = catalog.stickers
            .filter { it.type == StickerType.SPECIAL || it.section != null }
        val sections = specials.groupBy { it.section ?: "—" }
            .toSortedMap()
            .map { (section, stickers) ->
                SpecialsSection(
                    name = section,
                    rows = stickers.map { SpecialStickerRow(it, ownership[it.id] ?: 0) },
                )
            }
        SpecialsData(sections = sections)
    }
}

data class SpecialStickerRow(
    val sticker: Sticker,
    val quantity: Int,
)

data class SpecialsSection(
    val name: String,
    val rows: List<SpecialStickerRow>,
)

data class SpecialsData(
    val sections: List<SpecialsSection>,
) {
    companion object {
        val Empty = SpecialsData(sections = emptyList())
    }
}
