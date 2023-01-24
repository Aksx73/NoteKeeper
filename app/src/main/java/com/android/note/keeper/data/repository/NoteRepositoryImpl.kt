package com.android.note.keeper.data.repository

import com.android.note.keeper.data.local.NoteDao
import com.android.note.keeper.data.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 *  Implementation of [NoteRepository]. Single entry point for managing tasks data.
 */
class NoteRepositoryImpl @Inject constructor(private val dao: NoteDao) : NoteRepository {
    override fun getNotes(query: String): Flow<List<Note>> {
        return dao.getAllNotes(query)
    }

    override fun getArchiveNotes(query: String): Flow<List<Note>> {
        return dao.getAllArchiveNotes(query)
    }

    override suspend fun insertAndGetID(note: Note): Long {
        return dao.insertAndGetID(note)
    }

    override suspend fun insert(note: Note){
        return dao.insert(note)
    }

    override suspend fun getNoteById(id: Long) : Note {
        return dao.getNoteById(id)
    }

    override suspend fun getLastNote(): Note {
        return dao.getLastNote()
    }

    override suspend fun update(note: Note) = dao.update(note)

    override suspend fun delete(note: Note) = dao.delete(note)

   // override suspend fun updatePassword(id: Int, isProtected: Boolean) = dao.updatePassword(id, isProtected)

}