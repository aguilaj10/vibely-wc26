package com.vibely.wc26.domain.model

data class StickerCatalog(
    val version: String,
    val teams: List<Team>,
    val stickers: List<Sticker>,
) {
    val totalStickers: Int get() = stickers.size
    val totalTeams: Int get() = teams.size
    val totalSpecials: Int get() = stickers.count { it.type == StickerType.SPECIAL }
}
