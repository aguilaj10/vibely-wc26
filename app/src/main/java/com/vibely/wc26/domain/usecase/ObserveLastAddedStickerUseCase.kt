package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.model.Sticker
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Resolves the persisted "last added" sticker id back to a full [Sticker] by
 * looking it up in the catalog. Emits `null` until the user has added their
 * first sticker — or if the persisted id no longer matches the catalog (e.g.
 * after a catalog upgrade dropped a sticker).
 */
class ObserveLastAddedStickerUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val preferences: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Sticker?> = combine(
        flow { emit(catalogRepository.load()) },
        preferences.preferences,
    ) { catalog, prefs ->
        prefs.lastAddedStickerId?.let { id ->
            catalog.stickers.firstOrNull { it.id == id }
        }
    }
}
