package com.vibely.wc26.domain.model

/**
 * Progress numbers for a single scope (overall, one group, one team, or specials).
 * Pure data — derived fields stay computed to prevent drift.
 */
data class ProgressSummary(
    val owned: Int,              // stickers with quantity ≥ 1
    val total: Int,              // stickers in scope
    val duplicateStickers: Int,  // stickers with quantity ≥ 2
    val totalDuplicates: Int,    // sum of (quantity − 1) for each duplicated sticker
) {
    val missing: Int get() = (total - owned).coerceAtLeast(0)
    val progress: Float get() = if (total == 0) 0f else owned.toFloat() / total
    val isComplete: Boolean get() = total > 0 && owned == total
}

/**
 * Aggregate snapshot of the user's collection.
 * Keyed maps use group letter ("A".."L") and team code ("MEX", "CAN", ...).
 */
data class CollectionStats(
    val overall: ProgressSummary,
    val perGroup: Map<String, ProgressSummary>,
    val perTeam: Map<String, ProgressSummary>,
    val specials: ProgressSummary,
)
