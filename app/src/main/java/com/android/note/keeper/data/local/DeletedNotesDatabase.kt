package com.android.note.keeper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.note.keeper.data.model.DeletedNote

@Database(entities = [DeletedNote::class], version = 1)
abstract class DeletedNotesDatabase : RoomDatabase(){

    abstract fun deletedNotesDao() : DeletedNoteDao

}