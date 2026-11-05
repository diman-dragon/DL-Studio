package com.lingvadesk.app.di

import android.content.Context
import com.lingvadesk.app.data.local.LingvaDatabase
import com.lingvadesk.app.data.repository.LingvaRepositoryImpl
import com.lingvadesk.app.domain.repository.LingvaRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LingvaDatabase = LingvaDatabase.get(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(impl: LingvaRepositoryImpl): LingvaRepository
}
