package com.vibely.wc26.domain.model

data class Sticker(
    val id: String,            // "MEX01", "FWC1", "00"
    val teamCode: String?,     // null for specials
    val group: String?,        // null for specials
    val type: StickerType,
    val displayName: String,
    val section: String?,      // null for team stickers; "Section 1" etc. for specials
    val slotIndex: Int?,       // 1..20 for team stickers; null for specials
)
