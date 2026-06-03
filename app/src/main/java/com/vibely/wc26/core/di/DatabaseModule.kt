package com.vibely.wc26.core.di

import android.content.Context
import androidx.room.Room
import com.vibely.wc26.data.ownership.db.OwnershipDao
import com.vibely.wc26.data.ownership.db.OwnershipDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideOwnershipDatabase(
        @ApplicationContext context: Context,
    ): OwnershipDatabase = Room.databaseBuilder(
        context,
        OwnershipDatabase::class.java,
        "ownership.db",
    ).build()

    @Provides
    fun provideOwnershipDao(database: OwnershipDatabase): OwnershipDao =
        database.ownershipDao()
}
