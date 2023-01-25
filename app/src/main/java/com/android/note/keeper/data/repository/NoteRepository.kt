package com.android.note.keeper.data.repository

import com.android.note.keeper.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getNotes(query: String): Flow<List<Note>>

    fun getArchiveNotes(query: String): Flow<List<Note>>

    suspend fun insertAndGetID(note: Note) : Long

    suspend fun insert(note: Note)

    suspend fun getNoteById(id: Long) : Note

    suspend fun update(note: Note)

    suspend fun delete(note: Note)


}