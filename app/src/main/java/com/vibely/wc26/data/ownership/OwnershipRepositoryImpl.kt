package com.vibely.wc26.data.ownership

import com.vibely.wc26.data.ownership.db.OwnershipDao
import com.vibely.wc26.data.ownership.db.OwnershipEntity
import com.vibely.wc26.domain.ownership.OwnershipRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
internal class OwnershipRepositoryImpl @Inject constructor(
    private val dao: OwnershipDao,
) : OwnershipRepository {

    override fun observeAll(): Flow<Map<String, Int>> =
        dao.observeAll().map { rows -> rows.associate { it.stickerId to it.quantity } }

    override fun observeQuantity(stickerId: String): Flow<Int> =
        dao.observeQuantity(stickerId).map { it ?: 0 }

    override suspend fun getQuantity(stickerId: String): Int =
        dao.getQuantity(stickerId) ?: 0

    override suspend fun setQuantity(stickerId: String, quantity: Int) {
        if (quantity <= 0) {
            dao.delete(stickerId)
        } else {
            dao.upsert(
                OwnershipEntity(
                    stickerId = stickerId,
                    quantity = quantity,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    override suspend fun adjustQuantity(stickerId: String, delta: Int) {
        val current = dao.getQuantity(stickerId) ?: 0
        setQuantity(stickerId, current + delta)
    }

    override suspend fun clearAll() = dao.clear()
}
