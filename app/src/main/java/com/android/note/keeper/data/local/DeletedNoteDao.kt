package com.android.note.keeper.data.local

import androidx.room.*
import com.android.note.keeper.data.model.DeletedNote
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedNoteDao {

    @Query("SELECT * FROM deleted_notes_table ORDER BY created DESC")
    fun getAllDeletedNotes(): Flow<List<DeletedNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: DeletedNote)

    @Delete
    suspend fun delete(note: DeletedNote)

    @Query("DELETE FROM deleted_notes_table")
    suspend fun deleteAllNotes()
}
