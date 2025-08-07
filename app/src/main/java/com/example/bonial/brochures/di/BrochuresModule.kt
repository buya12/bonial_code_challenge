package com.example.bonial.brochures.di

import com.example.bonial.brochures.data.cache.BrochureCache
import com.example.bonial.brochures.data.repository.BrochuresRepositoryImpl
import com.example.bonial.brochures.data.source.ShelfService
import com.example.bonial.brochures.domain.repository.BrochuresRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(ViewModelComponent::class)
internal abstract class BrochuresModule {

    companion object {

        @Provides
        fun provideShelfService(retrofit: Retrofit): ShelfService {
            return retrofit.create()
        }

        @Provides
        fun provideShelfRepository(service: ShelfService, cache: BrochureCache): BrochuresRepository {
            return BrochuresRepositoryImpl(service, cache)
        }
    }
}
