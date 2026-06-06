package com.vibely.wc26.domain.usecase

import com.vibely.wc26.core.util.normalize
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.Team
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Live search across the catalog. Matches on display name, sticker id, OR team
 * name — the last one expands a team-name hit into the team's full 20-sticker
 * roster, which is how a collector typically searches ("show me all of Mexico").
 *
 * All comparisons are accent- and case-insensitive via [normalize]. Substring
 * match; no fuzzy scoring (that lives in [MatchScannedTextUseCase] for OCR).
 * 994 catalog entries → cheap enough to evaluate on every keystroke.
 *
 * Emits [SearchResultsData.Idle] for blank queries so the screen can show its
 * empty-prompt state without conflating it with "no matches".
 */
class ObserveSearchResultsUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val ownershipRepository: OwnershipRepository,
) {
    operator fun invoke(query: Flow<String>): Flow<SearchResultsData> = combine(
        flow { emit(catalogRepository.load()) },
        ownershipRepository.observeAll(),
        query,
    ) { catalog, ownership, raw ->
        val normalized = normalize(raw)
        if (normalized.isEmpty()) return@combine SearchResultsData.Idle

        val teamsByCode: Map<String, Team> = catalog.teams.associateBy { it.code }
        val teamHitCodes: Set<String> = catalog.teams
            .asSequence()
            .filter { normalize(it.name).contains(normalized) }
            .map { it.code }
            .toSet()

        val matches = catalog.stickers.filter { sticker ->
            normalize(sticker.displayName).contains(normalized) ||
                normalize(sticker.id).contains(normalized) ||
                (sticker.teamCode != null && sticker.teamCode in teamHitCodes)
        }

        if (matches.isEmpty()) return@combine SearchResultsData.NoMatches

        val groups = matches
            .groupBy { sticker -> sectionKey(sticker, teamsByCode) }
            .toSortedMap()
            .map { (label, stickers) ->
                SearchSection(
                    label = label,
                    rows = stickers
                        .sortedBy { it.slotIndex ?: Int.MAX_VALUE }
                        .map { sticker ->
                            SearchHit(
                                sticker = sticker,
                                team = sticker.teamCode?.let(teamsByCode::get),
                                quantity = ownership[sticker.id] ?: 0,
                            )
                        },
                )
            }
        SearchResultsData.Hits(sections = groups, totalCount = matches.size)
    }

    private fun sectionKey(sticker: Sticker, teamsByCode: Map<String, Team>): String =
        sticker.teamCode?.let { teamsByCode[it]?.name }
            ?: sticker.section
            ?: SECTION_OTHER

    private companion object {
        const val SECTION_OTHER = "—"
    }
}

data class SearchHit(
    val sticker: Sticker,
    val team: Team?,
    val quantity: Int,
)

data class SearchSection(
    val label: String,
    val rows: List<SearchHit>,
)

sealed interface SearchResultsData {
    /** Query is empty — show the friendly prompt, not "no matches". */
    data object Idle : SearchResultsData

    /** Query is non-empty but nothing matches. */
    data object NoMatches : SearchResultsData

    /** One or more matches, grouped by team or specials section. */
    data class Hits(val sections: List<SearchSection>, val totalCount: Int) : SearchResultsData
}
