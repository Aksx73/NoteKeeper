package com.android.note.keeper.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.util.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes_table WHERE archived = 0 AND ((title LIKE '%' || :searchQuery || '%') OR (content LIKE '%' || :searchQuery || '%')) ORDER BY created DESC") //ORDER BY pin DESC, created DESC
    fun getAllNotes(searchQuery: String): Flow<List<Note>>

    @Query("SELECT * FROM notes_table WHERE archived = 1 AND ((title LIKE '%' || :searchQuery || '%') OR (content LIKE '%' || :searchQuery || '%')) ORDER BY created DESC") //ORDER BY pin DESC, created DESC
    fun getAllArchiveNotes(searchQuery: String): Flow<List<Note>>

    @Insert(onConflict = REPLACE)
    suspend fun insertAndGetID(note: Note): Long

    @Insert(onConflict = REPLACE)
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes_table WHERE _id=:id")
    suspend fun getNoteById(id: Long): Note

    @Query("SELECT * FROM notes_table WHERE _id = (SELECT MAX(_id)  FROM notes_table)")
    suspend fun getLastNote(): Note

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

}