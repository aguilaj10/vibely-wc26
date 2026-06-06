package com.vibely.wc26.domain.usecase

import com.vibely.wc26.core.util.jaroWinkler
import com.vibely.wc26.core.util.normalize
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.StickerMatch
import javax.inject.Inject

/**
 * Match free-form text (OCR output or user input) against the catalog.
 *
 * Strategy:
 *   1. Exact sticker-ID hit (e.g. text contains "MEX02") → score 1.0
 *   2. Otherwise Jaro-Winkler against `displayName`, with a small bonus if the
 *      sticker's team name also appears in the text — disambiguates common
 *      surnames across teams.
 *
 * Returns top-N candidates with score ≥ [scoreFloor]. Empty list when nothing
 * passes the floor.
 */
class MatchScannedTextUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
) {

    suspend operator fun invoke(
        text: String,
        limit: Int = 3,
        scoreFloor: Float = 0.65f,
    ): List<StickerMatch> {
        if (text.isBlank()) return emptyList()

        val catalog = catalogRepository.load()
        val teamsByCode = catalog.teams.associateBy { it.code }

        val normalized = normalize(text)
        val tokens = normalized.split(' ').filter { it.length >= 3 }

        // 1) Exact sticker ID hit (e.g. "MEX02" present in OCR)
        val exact = catalog.stickers.firstOrNull { sticker ->
            // ID is a word, not just a substring — avoid "USA" matching "USA01"
            Regex("\\b${Regex.escape(sticker.id)}\\b").containsMatchIn(normalized)
        }
        if (exact != null) {
            return listOf(
                StickerMatch(
                    sticker = exact,
                    team = exact.teamCode?.let(teamsByCode::get),
                    score = 1f,
                )
            )
        }

        // 2) Fuzzy name match with optional team-name boost
        return catalog.stickers
            .asSequence()
            .map { sticker ->
                val team = sticker.teamCode?.let(teamsByCode::get)
                val nameNorm = normalize(sticker.displayName)
                val teamNorm = team?.name?.let(::normalize)

                val nameScore = bestTokenScore(nameNorm, tokens, fallback = normalized)
                val teamBonus =
                    if (teamNorm != null && tokens.any { jaroWinkler(teamNorm, it) >= 0.85 }) 0.1f
                    else 0f

                StickerMatch(
                    sticker = sticker,
                    team = team,
                    score = (nameScore + teamBonus).coerceAtMost(1f),
                )
            }
            .filter { it.score >= scoreFloor }
            .sortedByDescending { it.score }
            .take(limit)
            .toList()
    }

    private fun bestTokenScore(target: String, tokens: List<String>, fallback: String): Float {
        if (tokens.isEmpty()) return jaroWinkler(target, fallback).toFloat()
        var best = 0.0
        // Score against each token and also against the joined string — pick the higher.
        for (t in tokens) {
            val s = jaroWinkler(target, t)
            if (s > best) best = s
        }
        val joined = jaroWinkler(target, fallback)
        if (joined > best) best = joined
        return best.toFloat()
    }
}
