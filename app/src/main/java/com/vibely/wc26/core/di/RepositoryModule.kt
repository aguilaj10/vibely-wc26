package com.vibely.wc26.core.di

import com.vibely.wc26.data.catalog.CatalogRepositoryImpl
import com.vibely.wc26.data.ownership.OwnershipRepositoryImpl
import com.vibely.wc26.data.prefs.PrefsDataStore
import com.vibely.wc26.domain.catalog.CatalogRepository
import com.vibely.wc26.domain.ownership.OwnershipRepository
import com.vibely.wc26.domain.prefs.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindOwnershipRepository(impl: OwnershipRepositoryImpl): OwnershipRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: PrefsDataStore): UserPreferencesRepository
}
