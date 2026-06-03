package com.vibely.wc26.data.ownership.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface OwnershipDao {

    @Query("SELECT * FROM ownership")
    fun observeAll(): Flow<List<OwnershipEntity>>

    @Query("SELECT quantity FROM ownership WHERE stickerId = :id")
    fun observeQuantity(id: String): Flow<Int?>

    @Query("SELECT quantity FROM ownership WHERE stickerId = :id")
    suspend fun getQuantity(id: String): Int?

    @Upsert
    suspend fun upsert(entity: OwnershipEntity)

    @Query("DELETE FROM ownership WHERE stickerId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM ownership")
    suspend fun clear()
}
