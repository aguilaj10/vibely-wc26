package com.vibely.wc26.domain.model

/**
 * Result of matching OCR/search text against the catalog.
 * `score` is 0f..1f where 1f is an exact ID match.
 */
data class StickerMatch(
    val sticker: Sticker,
    val team: Team?,
    val score: Float,
)
