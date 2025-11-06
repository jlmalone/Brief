package com.techventus.wikipedianews.inject

import com.techventus.wikipedianews.model.repository.NewsRepository
import com.techventus.wikipedianews.model.repository.NewsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 * Following android-template pattern using @Binds for interface binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind NewsRepositoryImpl to NewsRepository interface.
     * Uses @Binds for efficient interface binding.
     */
    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        impl: NewsRepositoryImpl
    ): NewsRepository
}
