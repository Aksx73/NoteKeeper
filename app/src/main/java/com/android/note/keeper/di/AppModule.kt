package com.android.note.keeper.di

import android.app.Application
import androidx.room.Room
import com.android.note.keeper.data.local.DeletedNoteDao
import com.android.note.keeper.data.local.DeletedNotesDatabase
import com.android.note.keeper.data.local.NoteDao
import com.android.note.keeper.data.local.NoteDatabase
import com.android.note.keeper.data.repository.DeletedNoteRepository
import com.android.note.keeper.data.repository.DeletedNoteRepositoryImpl
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
    fun providesDeletedNotesDatabase(app: Application): DeletedNotesDatabase {
        return Room.databaseBuilder(
            app,
            DeletedNotesDatabase::class.java,
            Constants.DATABASE_DELETED_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesTaskDao(db: NoteDatabase): NoteDao {
        return db.noteDao()
    }

    @Provides
    @Singleton
    fun providesDeletedNotesDao(db:DeletedNotesDatabase):DeletedNoteDao{
        return db.deletedNotesDao()
    }

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(taskDao)
    }

    @Provides
    @Singleton
    fun provideDeletedTaskRepository(dao: DeletedNoteDao): DeletedNoteRepository {
        return DeletedNoteRepositoryImpl(dao)
    }

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope