package com.vibely.wc26.data.catalog

import android.content.Context
import com.vibely.wc26.data.catalog.dto.CatalogDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val ASSET_NAME = "stickers.json"

@Singleton
internal class CatalogDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun read(): CatalogDto = withContext(Dispatchers.IO) {
        val text = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        json.decodeFromString(CatalogDto.serializer(), text)
    }
}
