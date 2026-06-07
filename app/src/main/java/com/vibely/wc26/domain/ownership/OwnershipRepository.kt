package com.vibely.wc26.domain.ownership

import kotlinx.coroutines.flow.Flow

interface OwnershipRepository {
    /** Map of stickerId → quantity for every owned sticker (quantity ≥ 1). */
    fun observeAll(): Flow<Map<String, Int>>

    /** Quantity for a single sticker; emits 0 when not owned. */
    fun observeQuantity(stickerId: String): Flow<Int>

    /** One-shot read of a single sticker's quantity. Returns 0 when not owned. */
    suspend fun getQuantity(stickerId: String): Int

    /** Set quantity directly. quantity ≤ 0 removes the row. */
    suspend fun setQuantity(
        stickerId: String,
        quantity: Int,
    )

    /** Adjust quantity by delta (positive or negative). Floors at 0. */
    suspend fun adjustQuantity(
        stickerId: String,
        delta: Int,
    )

    /** Wipe all ownership data. */
    suspend fun clearAll()

    /** Bulk import quantities. Replaces existing data for stickers in [quantities]. */
    suspend fun importAll(quantities: Map<String, Int>)
}
