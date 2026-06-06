package com.vibely.wc26.domain.usecase

import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject

/**
 * Single entry point for mutating sticker quantity. Thin wrapper today; the
 * indirection exists so future side effects (last-added tracking, celebration
 * triggers, analytics) plug in here without touching ViewModels.
 */
class UpdateStickerQuantityUseCase @Inject constructor(
    private val ownershipRepository: OwnershipRepository,
) {
    /** Set absolute quantity. quantity ≤ 0 removes the row from ownership. */
    suspend fun set(stickerId: String, quantity: Int) {
        ownershipRepository.setQuantity(stickerId, quantity)
    }

    /** Add `delta` (positive or negative) to the current quantity. Floors at 0. */
    suspend fun adjust(stickerId: String, delta: Int) {
        ownershipRepository.adjustQuantity(stickerId, delta)
    }
}
