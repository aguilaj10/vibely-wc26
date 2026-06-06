package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.ownership.OwnershipRepository
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import javax.inject.Inject

/**
 * Single entry point for mutating sticker quantity. Records "last added" as a
 * side effect; celebrations are derived by host ViewModels from the team
 * progress they already observe, so no extra signal is needed here.
 */
class UpdateStickerQuantityUseCase @Inject constructor(
    private val ownershipRepository: OwnershipRepository,
    private val preferences: UserPreferencesRepository,
) {
    /** Set absolute quantity. quantity ≤ 0 removes the row from ownership. */
    suspend fun set(stickerId: String, quantity: Int) {
        val before = ownershipRepository.getQuantity(stickerId)
        ownershipRepository.setQuantity(stickerId, quantity)
        if (quantity > before) preferences.setLastAddedSticker(stickerId)
    }

    /** Add `delta` (positive or negative) to the current quantity. Floors at 0. */
    suspend fun adjust(stickerId: String, delta: Int) {
        ownershipRepository.adjustQuantity(stickerId, delta)
        if (delta > 0) preferences.setLastAddedSticker(stickerId)
    }
}
