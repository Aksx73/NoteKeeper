package com.android.note.keeper.di

import android.app.Application
import androidx.room.Room
import com.android.note.keeper.data.local.NoteDao
import com.android.note.keeper.data.local.NoteDatabase
import com.android.note.keeper.data.repository.NoteRepository
import com.android.note.keeper.data.repository.NoteRepositoryImpl
import com.android.note.keeper.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun providesDatabase(app: Application, callback: NoteDatabase.Callback): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
    }

    @Provides
    @Singleton
    fun providesTaskDao(db: NoteDatabase): NoteDao {
        return db.noteDao()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(taskDao)
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope