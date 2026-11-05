package com.lingvadesk.app.di

import android.content.Context
import androidx.room.Room
import com.lingvadesk.app.data.local.database.AppDatabase
import com.lingvadesk.app.data.local.database.LingvaDao
import com.lingvadesk.app.data.repository.LingvaRepositoryImpl
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.domain.usecase.SrsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lingocards.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideLingvaDao(db: AppDatabase): LingvaDao {
        return db.lingvaDao()
    }

    @Provides
    @Singleton
    fun provideLingvaRepository(dao: LingvaDao): LingvaRepository {
        return LingvaRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideSrsUseCase(): SrsUseCase {
        return SrsUseCase()
    }
}
