package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Emits the set of stickers the user does NOT yet own, grouped by team (one
 * section per team, plus a separate Specials section). Sections sort by group
 * letter then FIFA seed; stickers within a section sort by slot index so the
 * output mirrors how a collector walks the album page.
 *
 * Returns [MissingData] with an empty `sections` list when nothing is missing;
 * callers should render their own "all collected!" empty state.
 */
class ObserveMissingStickersUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
) {
    operator fun invoke(): Flow<MissingData> = combine(
        flow { emit(catalogRepository.load()) },
        ownershipRepository.observeAll(),
    ) { catalog, ownership ->
        val teamsByCode = catalog.teams.associateBy { it.code }
        val missing = catalog.stickers.filter { (ownership[it.id] ?: 0) == 0 }
        if (missing.isEmpty()) return@combine MissingData.Empty

        val teamSections = missing
            .filter { it.teamCode != null }
            .groupBy { it.teamCode!! }
            .mapNotNull { (code, stickers) ->
                val team = teamsByCode[code] ?: return@mapNotNull null
                TeamMissingSection(
                    teamCode = team.code,
                    teamName = team.name,
                    groupLetter = team.group,
                    seed = team.seed,
                    stickers = stickers.sortedBy { it.slotIndex ?: 0 },
                )
            }
            .sortedWith(compareBy({ it.groupLetter }, { it.seed }))
            .map { section ->
                MissingSection(
                    teamName = section.teamName,
                    isSpecials = false,
                    stickers = section.stickers,
                )
            }

        val specialsSection = missing
            .filter { it.teamCode == null }
            .takeIf { it.isNotEmpty() }
            ?.let { stickers ->
                MissingSection(
                    teamName = null,
                    isSpecials = true,
                    stickers = stickers.sortedBy { it.id },
                )
            }

        MissingData(
            sections = teamSections + listOfNotNull(specialsSection),
            totalMissing = missing.size,
        )
    }

    /** Intermediate shape used for sorting before exposing the public [MissingSection]. */
    private data class TeamMissingSection(
        val teamCode: String,
        val teamName: String,
        val groupLetter: String,
        val seed: Int,
        val stickers: List<Sticker>,
    )

}

data class MissingSection(
    /** Team display name; `null` when [isSpecials] is true. */
    val teamName: String?,
    val isSpecials: Boolean,
    val stickers: List<Sticker>,
)

data class MissingData(
    val sections: List<MissingSection>,
    val totalMissing: Int,
) {
    companion object {
        val Empty = MissingData(sections = emptyList(), totalMissing = 0)
    }
}
