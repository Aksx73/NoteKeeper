package com.android.note.keeper.data.repository

import com.android.note.keeper.data.model.DeletedNote
import kotlinx.coroutines.flow.Flow

interface DeletedNoteRepository {

    fun getDeletedNotes(): Flow<List<DeletedNote>>

    suspend fun insert(note: DeletedNote)

    suspend fun deleteAll()

    suspend fun delete(note: DeletedNote)
}