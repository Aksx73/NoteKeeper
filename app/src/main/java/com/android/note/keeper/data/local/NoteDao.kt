package com.android.note.keeper.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.android.note.keeper.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    //to update,add or remove password of note with given '_id'
    @Query("UPDATE Constants.notes_table SET password=:password WHERE _id=:id")
    suspend fun updatePassword(id: Int, password: String)

}