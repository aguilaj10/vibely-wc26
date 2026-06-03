package com.vibely.wc26.data.catalog

import com.vibely.wc26.data.catalog.dto.toDomain
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.StickerCatalog
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class CatalogRepositoryImpl @Inject constructor(
    private val dataSource: CatalogDataSource,
) : CatalogRepository {

    private val mutex = Mutex()
    @Volatile private var cached: StickerCatalog? = null

    override suspend fun load(): StickerCatalog {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: dataSource.read().toDomain().also { cached = it }
        }
    }
}
