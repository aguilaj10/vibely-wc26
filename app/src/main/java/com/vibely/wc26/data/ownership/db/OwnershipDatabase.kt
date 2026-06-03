package com.vibely.wc26.data.ownership.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [OwnershipEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class OwnershipDatabase : RoomDatabase() {
    abstract fun ownershipDao(): OwnershipDao
}
