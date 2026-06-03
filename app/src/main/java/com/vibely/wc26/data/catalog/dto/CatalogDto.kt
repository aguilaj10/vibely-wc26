package com.vibely.wc26.data.catalog.dto

import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.model.StickerCatalog
import com.vibely.wc26.domain.model.StickerType
import com.vibely.wc26.domain.model.Team
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire format of assets/stickers.json. Mirrors the structure produced by
 * tools/build_catalog.py — keep in sync if the JSON shape changes.
 */
@Serializable
internal data class CatalogDto(
    val version: String,
    val source: String? = null,
    @SerialName("generatedAt") val generatedAt: String? = null,
    val meta: CatalogMetaDto? = null,
    val teams: List<TeamDto>,
    val stickers: List<StickerDto>,
)

@Serializable
internal data class CatalogMetaDto(
    val totalStickers: Int,
    val totalTeams: Int,
    val totalSpecials: Int,
    val groups: List<String>,
)

@Serializable
internal data class TeamDto(
    val code: String,
    val name: String,
    val group: String,
    val seed: Int,
)

@Serializable
internal data class StickerDto(
    val id: String,
    val teamCode: String? = null,
    val group: String? = null,
    val type: StickerTypeDto,
    val displayName: String,
    val section: String? = null,
    val slotIndex: Int? = null,
)

@Serializable
internal enum class StickerTypeDto { BADGE, NORMAL, TEAM, SPECIAL }

internal fun StickerTypeDto.toDomain(): StickerType = when (this) {
    StickerTypeDto.BADGE -> StickerType.BADGE
    StickerTypeDto.NORMAL -> StickerType.NORMAL
    StickerTypeDto.TEAM -> StickerType.TEAM
    StickerTypeDto.SPECIAL -> StickerType.SPECIAL
}

internal fun TeamDto.toDomain(): Team = Team(code, name, group, seed)

internal fun StickerDto.toDomain(): Sticker = Sticker(
    id = id,
    teamCode = teamCode,
    group = group,
    type = type.toDomain(),
    displayName = displayName,
    section = section,
    slotIndex = slotIndex,
)

internal fun CatalogDto.toDomain(): StickerCatalog = StickerCatalog(
    version = version,
    teams = teams.map(TeamDto::toDomain),
    stickers = stickers.map(StickerDto::toDomain),
)
