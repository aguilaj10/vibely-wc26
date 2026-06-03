package com.vibely.wc26.domain.catalog

import com.vibely.wc26.domain.model.StickerCatalog

interface CatalogRepository {
    suspend fun load(): StickerCatalog
}
