package com.vibely.wc26.data.ownership.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ownership")
internal data class OwnershipEntity(
    @PrimaryKey val stickerId: String,
    val quantity: Int,
    val updatedAt: Long,
)
